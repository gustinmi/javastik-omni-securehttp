package com.gustinmi.ssltester;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.Principal;
import java.util.logging.Logger;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;

import com.gustinmi.cryptotest.cha10.SSLServerWithClientAuthIdExample;
import com.gustinmi.cryptotest.cha10.Utils;

/**
 * Basic SSL Server with optional client authentication.
 */
public class SslServer extends SSLServerWithClientAuthIdExample {

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

		System.out.println();
	}

	/**
	 * Send a response
	 */
	private static void sendResponse(OutputStream out) {
		PrintWriter pWrt = new PrintWriter(new OutputStreamWriter(out));
		pWrt.print("HTTP/1.1 200 OK\r\n");
		pWrt.print("Content-Type: text/html\r\n");
		pWrt.print("\r\n");
		pWrt.print("<html>\r\n");
		pWrt.print("<body>\r\n");
		pWrt.print("Hello World!\r\n");
		pWrt.print("</body>\r\n");
		pWrt.print("</html>\r\n");
		pWrt.flush();
	}

	public static void main(String[] args) throws Exception {

		log.info("Starting SSL server on port " + Utils.PORT_NO);

		// Get name and IP address of the local host
		try {
			InetAddress address = InetAddress.getLocalHost();
			log.info("Local Host:");
			log.info("\t" + address.getHostName());
			log.info("\t" + address.getHostAddress());
		} catch (UnknownHostException e) {
			System.out.println("Unable to determine this host's address");
		}

		SSLContext sslContext = createSSLContext();
		SSLServerSocketFactory fact = sslContext.getServerSocketFactory();
		SSLServerSocket sSock = (SSLServerSocket) fact.createServerSocket(Utils.PORT_NO);

		// client authenticate where possible
		sSock.setWantClientAuth(true);

		for (;;) {
			SSLSocket sslSock = (SSLSocket) sSock.accept();
			log.info("Client request received");
			
			 System.out.println("Handling client at " +
					 sslSock.getInetAddress().getHostAddress() + " on port " +
					 sslSock.getPort());

			
			try {
				sslSock.startHandshake();
			} catch (IOException e) {
				continue;
			}

			readRequest(sslSock.getInputStream());

			SSLSession session = sslSock.getSession();

			try {
				Principal clientID = session.getPeerPrincipal();

				log.info("client identified as: " + clientID);
			} catch (SSLPeerUnverifiedException e) {
				System.out.println("client not authenticated" + e.getMessage());
			}

			sendResponse(sslSock.getOutputStream());

			sslSock.close();
		}
	}
}