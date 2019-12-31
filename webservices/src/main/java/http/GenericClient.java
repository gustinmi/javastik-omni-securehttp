package http;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.ssl.HttpsURLConnection;


/** Instance based webservice client. For each webservice host (or server) you need one client.
 * <p>web services sometimes require HTTPS authentication so that's why client is per instance (CN - common name).
 * Communications link between the application and a URL 
 * @author gustinmi [at] gmail.com
 * @see java.net.URLConnection
 */
@Deprecated
public class GenericClient {
    
    public static enum HttpRequestType {
        GET,
        POST
    }
   
    // Fields
    
    public static final Logger log = Logger.getLogger(GenericClient.class.getCanonicalName());
    private volatile ClientSettings clientSettings;

    // Singleton with initialization (java boilerplate code)
    
    public static GenericClient instance = new GenericClient();
    private GenericClient() {}
    public void configure(ClientSettings clientSettings){
        
        log.finest("Attempting to create client with following settings: " + clientSettings);
        
        this.clientSettings = clientSettings;
    } 
    
    
    /** Prepares connection but does not actually connect on physical level
     * @param url
     * @return
     */
    private HttpURLConnection prepareConnection(final String url) {
        try {
            final HttpURLConnection connection;

                
            //                log.finest("PROXY Connecting to " + url);
            //                final Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(clientSettings.getProxyServer(), clientSettings.getProxyPort()));
            //                connection = (HttpURLConnection) new URL(url).openConnection(proxy);
            //                log.finest("Connected.");
            //                
                log.finest("NOPROXY Connecting to " + url);
                connection = (HttpURLConnection) new URL(url).openConnection(Proxy.NO_PROXY);
                log.finest("Connected.");
                


            if (connection instanceof HttpsURLConnection) {
                
                if (clientSettings.getKeystoreLocation() == null) throw new IllegalArgumentException("You're connecting to SSL server without proper SSL setup");
                
                //final ModulSSL modssl = new ModulSSL();
                //modssl.initialize(clientSettings);
                //modssl.configureConnectionForSsl(connection);
                //if (!modssl.isConfigured()) throw new IllegalStateException("You're using secure connection but SSL was not configured successfully!");
                
                log.finest("SSL handshake done. Secure connection is established.");
                
            }
            
            return connection;
        }
        catch (Exception e) {
            log.log(Level.SEVERE, "Error while connecting to url: " + url, e);
        }

        throw new IllegalStateException("Could not connect url: " + url);
        
    }
    
    
    /**
     * Creates request and connects. Below are the main steps in connecting to server 
     * <ol>
     *  <li>The connection object is created by invoking the openConnection method on a URL.
     *  <li>The setup parameters and general request properties are manipulated.
     *  <li>The actual connection to the remote object is made, using the connect method.
     *  <li>The remote object becomes available. The header fields and the contents of the remote object can be accessed. 
     * </ol>
     * @param textToSend any data in form of plain text (json, xml, txt, html)
     * @param url url to connect to
     * @return StringBuilder object with response data
     */
    public StringBuilder createPostRequest(final HttpRequestType httpRequestType, final String textToSend, final String url) {
        
        byte[] b = null;
        
        // 1. the connection object is created by invoking the openConnection method on a URL.
        final HttpURLConnection connection = prepareConnection(url);
        if (connection==null){
            log.severe("Connection to" + url + " cannot be estableshed!");
            throw new IllegalStateException("Can not continue with no active connection");
        }
        
        // Issue a HTTP request
        final StringBuilder outputString = new StringBuilder();
        try {
            log.finest("Issuing http request to server: " + url);
            log.finest("You're trying to send: " + textToSend);

            // 2. The setup parameters and general request properties are manipulated. Set the appropriate HTTP parameters.
            
            connection.setUseCaches(false); // disregrad http cache
            //connection.setRequestProperty("Content-Type", clientSettings.getHttpContentType()); 
            //connection.setRequestProperty("SOAPAction", "name"); // required by some webservice servers 
            connection.setRequestMethod(httpRequestType.name()); // usually a POST request is required for web services
            
            if (httpRequestType.equals(HttpRequestType.POST)){
             // Write input string to buffered output stream
                b = textToSend.getBytes();
                connection.setRequestProperty("Content-Length", String.valueOf(b.length));    
                connection.setDoOutput(true); // indicate we will write data to this url connection
            }
            
            // 3. The actual connection to the remote object is made, using the connect method
            connection.connect(); // actually connect

            //Write the content of the request to the output stream of the HTTP Connection.
            if (httpRequestType.equals(HttpRequestType.POST) && b!=null){
                final OutputStream out = connection.getOutputStream();
                out.write(b);
                out.close();
            }
            
        }catch (IOException e) { // if error is issued by server during request, get the error and log it 
            log.log(Level.SEVERE, "Fatal error while issuing request: ", e);
            final InputStream errorStream = connection.getErrorStream();
            final String errMsg = AppUtils.readFully(errorStream);
            log.log(Level.SEVERE, errMsg);
            return null;
        }finally {
            log.finest("Sucessfully send request.");
        }
        
        // 4. The remote object becomes available. (Read response or error info)
        String responseString;
        final InputStreamReader isr;
        try { // try to read the response.
            
            if (connection.getResponseCode() >= 400) 
                log.log(Level.SEVERE, "Server returned error code: " + connection.getResponseCode());
            
            isr = new InputStreamReader(connection.getInputStream());
            final BufferedReader in = new BufferedReader(isr);
            while ((responseString = in.readLine()) != null) { //Write the SOAP message response to a String.
                outputString.append(responseString);
            }
            
        }catch (IOException e) {
            log.log(Level.SEVERE, "Error while receiving response", e);
            final InputStream errorStream = connection.getErrorStream();
            final String errMsg = AppUtils.readFully(errorStream);
            log.log(Level.SEVERE, "Server returned following error: " + errMsg);
            return null;
        }
        
        log.finest("Sucessfully send request and retrieved following response: " + outputString);
        
        return outputString;
    }

} 
