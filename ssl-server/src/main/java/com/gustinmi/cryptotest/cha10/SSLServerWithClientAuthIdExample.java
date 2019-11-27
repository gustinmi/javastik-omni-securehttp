package com.gustinmi.cryptotest.cha10;

import static com.gustinmi.cryptotest.Utils.*;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;
import com.gustinmi.cryptotest.SSLContextFactoryBuilder;
import com.gustinmi.cryptotest.Validators;

/**
 * Basic SSL Server with client authentication and id checking.
 */
public class SSLServerWithClientAuthIdExample extends SSLServerExample {


	public static void main(String[] args) throws Exception {
		// create a context and set up a socket factory
        SSLContext sslContext = SSLContextFactoryBuilder.createServerSSLContext();

		// create the server socket
		SSLServerSocketFactory fact = sslContext.getServerSocketFactory();
        SSLServerSocket sSock = (SSLServerSocket) fact.createServerSocket(PORT_NO);

		sSock.setNeedClientAuth(true);

		SSLSocket sslSock = (SSLSocket) sSock.accept();

		sslSock.startHandshake();

		// process if principal checks out
        if (Validators.isEndEntity(sslSock.getSession(), "CN=Test End Certificate")) {
			doProtocol(sslSock);
		}
	}
}