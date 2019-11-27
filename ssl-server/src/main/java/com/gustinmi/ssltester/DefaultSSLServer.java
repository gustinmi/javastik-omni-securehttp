package com.gustinmi.ssltester;

import static com.gustinmi.cryptotest.Utils.*;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;
import com.gustinmi.cryptotest.cha10.SSLServerExample;

/**
 * Basic SSL Server with client authentication.
 * Uses default implementation / setup that came with java distribution 
 */
public class DefaultSSLServer extends SSLServerExample {

    public static void main(String[] args) throws Exception {
        final SSLServerSocketFactory fact = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
        final SSLServerSocket sSock = (SSLServerSocket) fact.createServerSocket(PORT_NO);
		sSock.setNeedClientAuth(true);
        final SSLSocket sslSock = (SSLSocket) sSock.accept();
        HttpProtocol.doProtocolServer(sslSock);
	}
    
}