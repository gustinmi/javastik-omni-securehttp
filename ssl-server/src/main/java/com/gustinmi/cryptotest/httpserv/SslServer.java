package com.gustinmi.cryptotest.httpserv;

import static com.gustinmi.cryptotest.Utils.*;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.ssl.*;
import com.gustinmi.cryptotest.Utils;
import com.gustinmi.cryptotest.certs.CertValidators;
import Utils.SslContextUtils;

/**
 * Basic SSL Server with optional client authentication.
 */
public class SslServer {

    /** If client authentication is required, you can check peer by peer and verify it's name, cn, organization */
    private static final boolean peerVerificationEnabled = false;

    public static final Logger log = Utils.loggerForThisClass();

	public static void main(String[] args) throws Exception {
	    
        if (new File(Utils.SERVER_NAME + ".jks").exists() == false || new File(Utils.TRUST_STORE_NAME + ".jks").exists() == false)
	        throw new IllegalStateException("Please create keystores first. Run CreateKeyStores.java");

        if (Utils.INFO_ENEABLED) log.info("Starting SSL server on port " + PORT_NO);

		// Get name and IP address of the local host
		try {
            final InetAddress address = InetAddress.getLocalHost();
            System.out.printf("Hostname: %s address %s \n", address.getHostName(), address.getHostAddress());
		} catch (UnknownHostException e) {
			System.out.println("Unable to determine this host's address");
		}

        final SSLContext sslContext = SslContextUtils.createSSLContextServer("server.jks", SERVER_PASSWORD, "trustStore.jks", TRUST_STORE_PASSWORD);
        final SSLServerSocketFactory fact = sslContext.getServerSocketFactory();
        final SSLServerSocket sSock = (SSLServerSocket) fact.createServerSocket(PORT_NO);

        sSock.setWantClientAuth(true); // client authenticate where possible but not required (CIA HIVE used this option)
        // sSock.setNeedClientAuth(true); // client authenticate is required

        for (;;) { // run forever

            final SSLSocket sslSock = (SSLSocket) sSock.accept();
			
            if (Utils.SYSOUT_ENABLED) System.out.printf("Handling client at %s on port %s \n", sslSock.getInetAddress().getHostAddress(), sslSock.getPort());
			
			try {

				sslSock.startHandshake();

			} catch (IOException e) {
                log.log(Level.SEVERE, "Error in SSL handshake " + e.getMessage(), e);
                continue; // continue serving, do not stop if faulty client connects
			}

            if (peerVerificationEnabled) {
                log.log(Level.INFO, "Validating peer");
                try {
                    // process if principal checks out
                    CertValidators.isEndEntity(sslSock.getSession(), "CN=Test End Certificate");

                } catch (SSLPeerUnverifiedException e) {
                    continue; // continue serving, do not stop
                }
            }

            try {

                HttpProtocol.doSslServerProtocol(sslSock);

            } catch (IOException e) {
                log.log(Level.SEVERE, e.getMessage(), e);
                continue; // continue serving, do not stop if faulty client connects
            }

		}
	}
}