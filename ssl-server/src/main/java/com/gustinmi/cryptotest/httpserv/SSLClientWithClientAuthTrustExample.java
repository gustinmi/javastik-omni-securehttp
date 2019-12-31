package com.gustinmi.cryptotest.httpserv;

import static com.gustinmi.cryptotest.Utils.*;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import Utils.SslContextUtils;

/**
 * SSL Client with client-side authentication.
 */
public class SSLClientWithClientAuthTrustExample {

	public static void main(String[] args) throws Exception {

        SSLContext sslContext = SslContextUtils.createSSLContextClient();
		SSLSocketFactory fact = sslContext.getSocketFactory();

        SSLSocket clientSocket = (SSLSocket) fact.createSocket(HOST, PORT_NO);

        HttpProtocol.doProtocolClient(clientSocket);

        //doProtocol(cSock);
	}
}
