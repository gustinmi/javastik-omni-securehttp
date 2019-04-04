package com.gustinmi.ssltester;

import java.io.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.Principal;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.ssl.*;
import com.gustinmi.cryptotest.cha10.Utils;

/**
 * Basic SSL Server with optional client authentication.
 */
public class SslServer {

	public static final Logger log = Utils.loggerForThisClass();

	/**
	 * Read a HTTP request
	 */
	private static void readRequest(InputStream in) throws IOException {
		System.out.print("Request: ");
		int ch = 0;
		int lastCh = 0;
		while ((ch = in.read()) >= 0 && (ch != '\n' && lastCh != '\n')) {
			System.out.print((char) ch);
			if (ch != '\r')
				lastCh = ch;
		}
        System.out.println("end of request.");
	}

	/**
	 * Send a response
	 */
	private static void sendResponse(OutputStream out) {
		PrintWriter pWrt = new PrintWriter(new OutputStreamWriter(out));
        // HTTP head
		pWrt.print("HTTP/1.1 200 OK\r\n");
		pWrt.print("Content-Type: text/html\r\n");
        // empty line delimiter for body
		pWrt.print("\r\n");
        // HTTP request body
		pWrt.print("<html>\r\n");
		pWrt.print("<body>\r\n");
        pWrt.print("Hello from SSL server \r\n");
		pWrt.print("</body>\r\n");
		pWrt.print("</html>\r\n");
		pWrt.flush();
	}

	public static void main(String[] args) throws Exception {
	    
	    if (new File("server.jks").exists() == false || new File("trustStore.jks").exists() == false)
	        throw new IllegalStateException("Please create keystores first. Run CreateKeyStores.java");
	    

		log.info("Starting SSL server on port " + Utils.PORT_NO);

		// Get name and IP address of the local host
		try {
            final InetAddress address = InetAddress.getLocalHost();
			log.info("Local Host:");
			log.info("\t" + address.getHostName());
			log.info("\t" + address.getHostAddress());
		} catch (UnknownHostException e) {
			System.out.println("Unable to determine this host's address");
		}

        final SSLContext sslContext = SslContextFactory.createSSLContext("server.jks", Utils.SERVER_PASSWORD, "trustStore.jks", Utils.TRUST_STORE_PASSWORD);
        final SSLServerSocketFactory fact = sslContext.getServerSocketFactory();
        final SSLServerSocket sSock = (SSLServerSocket) fact.createServerSocket(Utils.PORT_NO);

        // client authenticate where possible but not required
		sSock.setWantClientAuth(true);

        for (;;) { // run forever

            final SSLSocket sslSock = (SSLSocket) sSock.accept();
			log.info("Client request received");
			
            System.out.println("Handling client at " +
					 sslSock.getInetAddress().getHostAddress() + " on port " +
					 sslSock.getPort());
			
			try {
				sslSock.startHandshake();
			} catch (IOException e) {
                log.log(Level.SEVERE, e.getMessage(), e);
                continue; // continue serving, do not stop if faulty client connects
			}

            //try (final InputStream inputStream = sslSock.getInputStream()) {
                readRequest(sslSock.getInputStream());
            //}

            final SSLSession session = sslSock.getSession();

			try {
                final Principal clientID = session.getPeerPrincipal();
				log.info("client identified as: " + clientID);
			} catch (SSLPeerUnverifiedException e) {
				System.out.println("client not authenticated" + e.getMessage());
			}

            //try (final OutputStream outputStream = sslSock.getOutputStream()) {
            sendResponse(sslSock.getOutputStream());
            sslSock.close(); // end client request        
            //}


		}
	}
}