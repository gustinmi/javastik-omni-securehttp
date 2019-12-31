package com.gustinmi.cryptotest.httpserv;

import static com.gustinmi.cryptotest.Utils.*;
import java.io.*;
import java.net.Socket;
import java.security.Principal;
import java.util.Date;
import java.util.logging.Logger;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import com.gustinmi.cryptotest.Utils;

/** HTTP Protocol implementation
 * Provides secure or insecure wrapper
 * Provides methods for sending and receiving http 
 * @author gustin
 *
 */
public class HttpProtocol {

    public static final Logger log = Utils.loggerForThisClass();

    // Server side protocol (read request, send rersponse)

    /** Normal socket (no SSL utilities, wrappers) */
    public static void doUnsecureServerProtocol(Socket socket) throws IOException {

        try (final InputStream inputStream = socket.getInputStream()) {

            HttpProtocol.readRequestFromClient(inputStream);

            try (final OutputStream outputStream = socket.getOutputStream()) {
                HttpProtocol.sendResponseToClient(outputStream);
            }

        }
        finally {
            try {
                socket.close(); // end client request       
            } catch (Exception ex) {}
        }

    }

    /** Read request, send reponse - Socket with SSL context */
    public static void doSslServerProtocol(SSLSocket sslSock) throws IOException {

        try (final InputStream inputStream = sslSock.getInputStream()) {

            HttpProtocol.readRequestFromClient(inputStream); // read HTTP request

            final SSLSession session = sslSock.getSession();

            try {
                final Principal clientID = session.getPeerPrincipal();
                if (Utils.INFO_ENEABLED) log.info("client identified as: " + clientID);
            } catch (SSLPeerUnverifiedException e) {
                if (Utils.SYSOUT_ENABLED) System.out.println("client not authenticated");
            }

            try (final OutputStream outputStream = sslSock.getOutputStream()) {

                HttpProtocol.sendResponseToClient(outputStream);

            }

        }
        finally {
            try {
                sslSock.close(); // end client request       
            } catch (Exception ex) {}
        }

    }

    /**
     * Read a HTTP request (written by client to server)
     */
    public static void readRequestFromClient(InputStream in) throws IOException {

        if (Utils.SYSOUT_ENABLED) System.out.print("HTTP Request: ");
        int ch = 0;
        int lastCh = 0;
        while ((ch = in.read()) >= 0 && (ch != '\n' && lastCh != '\n')) {
            if (Utils.SYSOUT_ENABLED) {
                System.out.print((char) ch);
            }

            if (ch != '\r') lastCh = ch;   // http procols end with 2 empty lines
        }
        if (Utils.SYSOUT_ENABLED) System.out.println("end of request.");

    }

    /**
     * Send a response
     */
    public static void sendResponseToClient(OutputStream out) {

        // Response to send
        StringBuilder sb = new StringBuilder();
        sb.append("<html>\r\n");
        sb.append("<body>\r\n");
        sb.append(String.format("Hello from server. Time on server is %s \r\n", new Date().toString()));
        sb.append("</body>\r\n");
        sb.append("</html>\r\n");

        final PrintWriter pWrt = new PrintWriter(new OutputStreamWriter(out));

        // HTTP head
        pWrt.print("HTTP/1.1 200 OK\r\n");
        pWrt.print("Content-Type: text/html\r\n");
        pWrt.print("Content-Length: " + sb.length() + "\r\n"); // length is always body length

        // empty line delimiter for body
        pWrt.print("\r\n");

        // HTTP request body
        pWrt.print(sb.toString());
        pWrt.flush();
    }

    // Client side protocol

    public static void doProtocolClient(Socket socket) throws IOException {

        try (final OutputStream outputStream = socket.getOutputStream()) {

            outputStream.write(toByteArray("GET / HTTP/1.1"));
            outputStream.write('\n');

            try (final InputStream inputStream = socket.getInputStream()) {
                readRequestFromClient(inputStream);
            }

        } catch (IOException ex) {
            ex.printStackTrace();
        }



    }

    public static void readResponseFromServer(InputStream in) throws IOException {

        if (Utils.SYSOUT_ENABLED) System.out.print("HTTP Response: ");
        int ch = 0;
        int lastCh = 0;
        while ((ch = in.read()) >= 0 && (ch != '\n' && lastCh != '\n')) {
            if (Utils.SYSOUT_ENABLED) {
                System.out.print((char) ch);
            }

            if (ch != '\r') lastCh = ch;   // http procols end with 2 empty lines
        }
        if (Utils.SYSOUT_ENABLED) System.out.println("end of request.");

    }

}
