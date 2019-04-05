package com.gustinmi.ssltester;

import static com.gustinmi.cryptotest.Flags.*;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;
import com.gustinmi.cryptotest.cha10.Utils;

/**
 * Basic SSL Server with optional client authentication.
 */
public class SslServer {

	public static final Logger log = Utils.loggerForThisClass();

	public static void main(String[] args) throws Exception {
	    
	    if (new File("server.jks").exists() == false || new File("trustStore.jks").exists() == false)
	        throw new IllegalStateException("Please create keystores first. Run CreateKeyStores.java");

        if (INFO_ENEABLED) log.info("Starting SSL server on port " + Utils.PORT_NO);

		// Get name and IP address of the local host
		try {
            final InetAddress address = InetAddress.getLocalHost();
            System.out.printf("Hostname: %s address %s \n", address.getHostName(), address.getHostAddress());
		} catch (UnknownHostException e) {
			System.out.println("Unable to determine this host's address");
		}

        final SSLContext sslContext = SslContextFactory.createSSLContext("server.jks", Utils.SERVER_PASSWORD, "trustStore.jks", Utils.TRUST_STORE_PASSWORD);
        final SSLServerSocketFactory fact = sslContext.getServerSocketFactory();
        final SSLServerSocket sSock = (SSLServerSocket) fact.createServerSocket(Utils.PORT_NO);
        sSock.setWantClientAuth(true); // client authenticate where possible but not required

        for (;;) { // run forever

            final SSLSocket sslSock = (SSLSocket) sSock.accept();
			
            if (SYSOUT_ENABLED) System.out.printf("Handling client at %s on port %s \n", sslSock.getInetAddress().getHostAddress(), sslSock.getPort());
			
			try {
				sslSock.startHandshake();
			} catch (IOException e) {
                log.log(Level.SEVERE, e.getMessage(), e);
                continue; // continue serving, do not stop if faulty client connects
			}

            try {
                HttpProtocol.doProtocol(sslSock);
            } catch (IOException e) {
                log.log(Level.SEVERE, e.getMessage(), e);
                continue; // continue serving, do not stop if faulty client connects
            }

		}
	}
}