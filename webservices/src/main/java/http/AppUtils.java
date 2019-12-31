package http;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AppUtils {
    
    public static final Logger Log = Logger.getLogger(AppUtils.class.getCanonicalName());

    public static String ENCODING = "UTF-8";

    public static InputStream stringToStream(String text) {
        final InputStream stream = new ByteArrayInputStream(text.getBytes(StandardCharsets.UTF_8));
        return stream;
    }

    public static void copyStream(final OutputStream out, final InputStream in) throws java.io.IOException {
        final byte[] buffer = new byte[8192];
        int bytes;
        while ((bytes = in.read(buffer)) != -1) {
            out.write(buffer, 0, bytes);
        }
    }

    /** Reads data as string */
    public static String readStringFully(InputStream inputStream) {
        try (final BufferedReader in = new BufferedReader(new InputStreamReader(inputStream, ENCODING));) {
            StringBuilder builder = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) {
                builder.append(line);
                builder.append("\n");
            }
            return builder.toString();
        } catch (IOException e) {
            Log.log(Level.SEVERE, "Error reading stream.", e);
        }

        throw new IllegalStateException("Napaka pri branju streama!");
    }

    public static String printFile(File f) throws FileNotFoundException, IOException {
        StringBuilder buff = new StringBuilder(1000);
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line = null;
            while ((line = br.readLine()) != null) {
                buff.append(line);
                buff.append("\n");
            }
        }
        return buff.toString();
    }

    /** Reads binary data */
    public static String readFully(InputStream inputStream) {
        byte[] buffer = new byte[1024];
        int length = 0;
        try (final ByteArrayOutputStream baos = new ByteArrayOutputStream();) {
            while ((length = inputStream.read(buffer)) != -1) {
                baos.write(buffer, 0, length);
            }
            return baos.toString();
        } catch (IOException e) {
            Log.log(Level.SEVERE, "Error reading stream.", e);
        }

        throw new IllegalStateException("Napaka pri branju streama!");
    }

    /**
     * Removes soap envelope to prepare response for JAXB serialization
     * @param src
     * @return response text without SOAP XML stuff
     */
    public static String removeSoapEnvelopeXml(String src) {
        
        // we remove SOAP envelope code by creating new temporary file in memory
        
        final StringBuilder sb = new StringBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        sb.append(src, src.indexOf("<soap:Body>") + 11, src.lastIndexOf("</soap:Body>"));
        
        return sb.toString();
    }
    
}
