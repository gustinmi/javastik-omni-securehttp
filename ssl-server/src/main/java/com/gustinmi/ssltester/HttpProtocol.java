package com.gustinmi.ssltester;

import static com.gustinmi.cryptotest.Flags.*;
import java.io.*;
import java.security.Principal;
import java.util.Date;
import java.util.logging.Logger;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import com.gustinmi.cryptotest.cha10.Utils;

/** HTTP Protocol specifics 
 * @author gustin
 *
 */
public class HttpProtocol {

    public static final Logger log = Utils.loggerForThisClass();

    public static void doProtocol(SSLSocket sslSock) throws IOException {

        try (final InputStream inputStream = sslSock.getInputStream()) {

            HttpProtocol.readRequest(inputStream);

            final SSLSession session = sslSock.getSession();

            try {
                final Principal clientID = session.getPeerPrincipal();
                if (INFO_ENEABLED) log.info("client identified as: " + clientID);
            } catch (SSLPeerUnverifiedException e) {
                if (SYSOUT_ENABLED) System.out.println("client not authenticated");
            }

            try (final OutputStream outputStream = sslSock.getOutputStream()) {
                HttpProtocol.sendResponse(outputStream);
            }

        }
        finally {
            try {
                if (!sslSock.isClosed()) sslSock.close(); // end client request       
            } catch (Exception ex) {}
        }

    }

    /**
     * Read a HTTP request
     */
    public static void readRequest(InputStream in) throws IOException {
        if (SYSOUT_ENABLED) System.out.print("Request: ");
        int ch = 0;
        int lastCh = 0;
        while ((ch = in.read()) >= 0 && (ch != '\n' && lastCh != '\n')) {
            if (SYSOUT_ENABLED) System.out.print((char) ch);
            if (ch != '\r') lastCh = ch;
        }
        if (SYSOUT_ENABLED) System.out.println("end of request.");
    }

    /**
     * Send a response
     */
    public static void sendResponse(OutputStream out) {
        PrintWriter pWrt = new PrintWriter(new OutputStreamWriter(out));
        // HTTP head
        pWrt.print("HTTP/1.1 200 OK\r\n");
        pWrt.print("Content-Type: text/html\r\n");
        // empty line delimiter for body
        pWrt.print("\r\n");
        // HTTP request body
        pWrt.print("<html>\r\n");
        pWrt.print("<body>\r\n");
        pWrt.printf("Hello from SSL server. Time on server is %s \r\n", new Date().toString());
        pWrt.print("</body>\r\n");
        pWrt.print("</html>\r\n");
        pWrt.flush();
    }

}
