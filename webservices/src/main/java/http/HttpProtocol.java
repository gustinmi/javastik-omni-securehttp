package http;

import static http.AppUtils.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import http.WebAr.HttpRequestType;
import http.WebAr.Response;
import http.WebAr.TimeoutDefs;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public class HttpProtocol {

    public static final Logger Log = Logger.getLogger(HttpProtocol.class.getCanonicalName());
    
    public static final String MULTIPART_CONTENT_TYPE = "multipart/form-data; charset=UTF-8; boundary=%s";
    public static final String JSON_CONTENT_TYPE = "application/json;charset=UTF-8";

    /** Multipart Boundary as per https://www.w3.org/Protocols/rfc1341/7_2_Multipart.html */
    public static final String BOUNDARY = UUID.randomUUID().toString();
    public static final byte[] BOUNDARY_PART = ("--" + BOUNDARY + "\r\n").getBytes(StandardCharsets.UTF_8);
    public static final byte[] FINISH_BOUNDARY = ("--" + BOUNDARY + "--\r\n").getBytes(StandardCharsets.UTF_8);
    
    public static Response makeRequest(final TimeoutDefs timeoutDefs, final HttpRequestType httpMethod, final HttpURLConnection connection, String rawData, WebAr.KeyValueData[] formData, WebAr.UploadData[] files) throws Exception {

        final String contentType;
        final String length;
        final File requestContentFile;
        final WebAr.HttpRequestFormat requestFormat = HttpRequestType.get(rawData, formData, files);
        switch (requestFormat) {
            case FORM_DATA:
                throw new NotImplementedException();
            case MULTIPART:
                requestContentFile = buildMultipart(formData, files);
                length = Long.toString(requestContentFile.length());
                contentType = String.format(MULTIPART_CONTENT_TYPE, BOUNDARY);
                break;
            case NODATA:
                length = null;
                requestContentFile = null;
                contentType = null;
                break;
            case RAW:
                requestContentFile = buildRaw(rawData);
                length = Integer.toString(rawData.length());
                contentType = JSON_CONTENT_TYPE;
                break;
            default:
                throw new IllegalArgumentException("Option " + requestFormat + " is not supported");
        }
        
        
        
        try {

            try {

                connection.setRequestProperty("Content-Type", contentType);
                connection.setRequestProperty("Content-Length", length);

                if (connection.getURL().getUserInfo() != null) connection.setRequestProperty("Authorization", "Basic " + Base64.encodeBytes(connection.getURL().getUserInfo().getBytes()));

                connection.setConnectTimeout(timeoutDefs.getConnectTimeout());
                connection.setReadTimeout(timeoutDefs.getConnectTimeout());

                // setup parameters
                if (requestFormat.equals(WebAr.HttpRequestFormat.FORM_DATA) || requestFormat.equals(WebAr.HttpRequestFormat.MULTIPART) || requestFormat.equals(WebAr.HttpRequestFormat.RAW)) // we have some data other then url and headers
                    connection.setDoOutput(true);

                // general request properties
                connection.setDoInput(true);
                connection.setRequestMethod(httpMethod.name());
                connection.setUseCaches(false); // disregrad http cache

                // This method is used to enable streaming of a HTTP request body without internal buffering, when the content length is not known in advance.
                //connection.setChunkedStreamingMode(0); // Enable streaming mode with default settings

            } catch (Exception e) {
                Log.log(Level.SEVERE, "Error while preparing to connect" + e.getMessage(), e);
                throw e;
            }

            try {

                final StringBuilder outputString = new StringBuilder(1000);

                // send payload
                if (connection.getDoOutput()) {
                    try (final OutputStream out = connection.getOutputStream()) {
                        try (final InputStream tempIn = new FileInputStream(requestContentFile)) {
                            copyStream(out, tempIn);
                        }
                    }
                }

                // receive response
                final int statusCode = connection.getResponseCode();

                if (connection.getDoInput()) {

                    if (statusCode >= 400) {

                        // get the error
                        try (final InputStreamReader errorStream = new InputStreamReader(connection.getErrorStream())) {
                            String responseString;
                            try (final BufferedReader in = new BufferedReader(errorStream)) {
                                while ((responseString = in.readLine()) != null) {
                                    outputString.append(responseString);
                                }
                            }

                            Log.info("Got error message");

                        }

                        throw new ProtocolException(String.format("Unsuccessful HTTP status code: %s from %s %s", statusCode, httpMethod.name(), connection.getURL().toString()));

                    } else {

                        // get response 
                        try (final InputStreamReader respStream = new InputStreamReader(connection.getInputStream())) {

                            String responseString;
                            try (final BufferedReader in = new BufferedReader(respStream)) {
                                while ((responseString = in.readLine()) != null) {
                                    outputString.append(responseString);
                                }
                            }

                            Log.info("Response data was returned");
                        }

                    }

                }

                final Map<String, List<String>> responseHeaders = connection.getHeaderFields();

                return new Response(statusCode, outputString.toString(), connection, responseHeaders);

            } catch (IOException e) {
                final StringBuilder outputString = new StringBuilder(1000);
                Log.log(Level.SEVERE, "Error while connecting to " + connection.getURL().toString() + ". Error is " + e.getMessage(), e);

                try (final InputStreamReader errorStream = new InputStreamReader(connection.getErrorStream())) {
                    String responseString;
                    try (final BufferedReader in = new BufferedReader(errorStream)) {
                        while ((responseString = in.readLine()) != null) {
                            outputString.append(responseString);
                        }
                    }
                    Log.log(Level.SEVERE, outputString.toString());
                } catch (IOException e1) {
                    Log.log(Level.SEVERE, "Error reading error stream: ", e1);
                }

                throw e;
            }
            finally {
                connection.disconnect(); // zapri connection
            }
        }
        finally {
            requestContentFile.delete();
        }
    }

    public static File buildRaw(String rawData) throws IOException {
        final File tempFile = File.createTempFile("multipart", "dat"); // temporary file storage, to track content length without memory buffers 
        try (final FileWriter fileWriter = new FileWriter(tempFile)) {
            fileWriter.write(rawData);
        }
        return tempFile;
    }

    public static File buildMultipart(WebAr.KeyValueData[] formData, WebAr.UploadData[] files) throws IOException {

        final File tempFile = File.createTempFile("multipart", "dat"); // temporary file storage, to track content length without memory buffers 

        try (final OutputStream tmpOut = new FileOutputStream(tempFile)) {
            //tmpOut.write(BOUNDARY_PART); // BOUNDARY
            for (WebAr.KeyValueData data : formData) { // WRITING FORM DATA
                tmpOut.write(BOUNDARY_PART); // BOUNDARY
                sendField(tmpOut, data.getName(), data.getValue()); // Send our first field
            }
            for (WebAr.UploadData file : files) { // Send FILE 1
                tmpOut.write(BOUNDARY_PART); // BOUNDARY
                sendFile(tmpOut, file.getName(), file.getContent(), file.getName());

            }
            tmpOut.write(FINISH_BOUNDARY); // BOUNDARY
        }

        return tempFile;
    }

    /** Metoda naredi multipart delm z vsebino datoteke.
     * Header in body sta ločena z prazno vrstico
     * Vse vrstice so končane  
     * @param out
     * @param name
     * @param in
     * @param fileName
     * @throws IOException
     */
    private static void sendFile(OutputStream out, String name, InputStream in, String fileName) throws IOException {

        String o = "Content-Disposition: form-data; name=\"" + URLEncoder.encode(name, "UTF-8") + "\"; filename=\"" + URLEncoder.encode(fileName, "UTF-8") + "\"\r\n";
        out.write(o.getBytes(StandardCharsets.UTF_8));
        out.write("Content-Type: application/vnd.openxmlformats-officedocument.wordprocessingml.document\r\n".getBytes(StandardCharsets.UTF_8));
        out.write("Content-Transfer-Encoding: binary\r\n".getBytes(StandardCharsets.UTF_8));
        out.write("\r\n".getBytes(StandardCharsets.UTF_8)); // konec form fieldaa new line
        byte[] buffer = new byte[2048];
        for (int n = 0; n >= 0; n = in.read(buffer))
            out.write(buffer, 0, n);
        out.write("\r\n".getBytes(StandardCharsets.UTF_8)); // konec file new line

    }

    /** Metoda naredi part z form fieldom
     * Header in body sta ločena z prazno vrstico
     * Vse vrstice so končane   
     * @param out
     * @param name
     * @param field
     * @throws IOException
     */
    private static void sendField(OutputStream out, String name, String field) throws IOException {
        String o = "Content-Disposition: form-data; name=\"" + URLEncoder.encode(name, "UTF-8") + "\"\r\n";
        out.write(o.getBytes(StandardCharsets.UTF_8));
        out.write("Content-Type: text/plain; charset=UTF-8\r\n".getBytes(StandardCharsets.UTF_8));
        out.write("Content-Transfer-Encoding: 8bit\r\n".getBytes(StandardCharsets.UTF_8));
        out.write("\r\n".getBytes(StandardCharsets.UTF_8)); // end of header must have empty line
        out.write(URLEncoder.encode(field, "UTF-8").getBytes(StandardCharsets.UTF_8));
        out.write("\r\n".getBytes(StandardCharsets.UTF_8)); // end of form field - line break 
    }

    /** Metoda naredi part z form fieldom
     * Header in body sta ločena z prazno vrstico
     * Vse vrstice so končane   
     * @param out
     * @param name
     * @param field
     * @throws IOException
     */
    private static void sendRaw(OutputStream out, String data) throws IOException {
        out.write("Content-Type: application/json; charset=UTF-8\r\n".getBytes(StandardCharsets.UTF_8));
        out.write("\r\n".getBytes(StandardCharsets.UTF_8)); // end of header must have empty line
        out.write(URLEncoder.encode(data, "UTF-8").getBytes(StandardCharsets.UTF_8));
        out.write("\r\n".getBytes(StandardCharsets.UTF_8)); // end of form field - line break 
    }


}
