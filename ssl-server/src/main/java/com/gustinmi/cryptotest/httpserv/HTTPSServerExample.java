package com.gustinmi.cryptotest.httpserv;

import static com.gustinmi.cryptotest.Utils.*;
import static com.gustinmi.cryptotest.httpserv.HttpProtocol.*;
import java.io.IOException;
import java.security.Principal;
import javax.net.ssl.*;

/**
 * Basic SSL Server with optional client authentication.
 */
public class HTTPSServerExample extends SSLServerWithClientAuthIdExample {


	public static void main(String[] args) throws Exception {
        SSLContext sslContext = SSLContextFactoryBuilder.createServerSSLContext();
		SSLServerSocketFactory fact = sslContext.getServerSocketFactory();
        SSLServerSocket sSock = (SSLServerSocket) fact.createServerSocket(PORT_NO);

		// client authenticate where possible
		sSock.setWantClientAuth(true);

		for (;;) {
			SSLSocket sslSock = (SSLSocket) sSock.accept();

			try {
				sslSock.startHandshake();
			} catch (IOException e) {
				continue;
			}

			readRequestFromClient(sslSock.getInputStream());

			SSLSession session = sslSock.getSession();

			try {
				Principal clientID = session.getPeerPrincipal();

				System.out.println("client identified as: " + clientID);
			} catch (SSLPeerUnverifiedException e) {
				System.out.println("client not authenticated");
			}

			sendResponseToClient(sslSock.getOutputStream());

			sslSock.close();
		}
	}
}