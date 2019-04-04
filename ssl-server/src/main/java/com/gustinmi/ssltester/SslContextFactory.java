package com.gustinmi.ssltester;

import java.io.FileInputStream;
import java.security.KeyStore;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

public class SslContextFactory {

    /**
     * Create an SSL context with identity and trust stores in place.
     * Identity : Who am I
     * Trust : who do I trust
     */
    protected static SSLContext createSSLContext(String serverJksPath, char[] jksPassword, String trustKeystorepath, char[] trustPassword) throws Exception {

        // IDENTITY - Who am i

        // set up a key manager for our local credentials
        KeyManagerFactory mgrFact = KeyManagerFactory.getInstance("SunX509");
        KeyStore serverStore = KeyStore.getInstance("JKS");
        serverStore.load(new FileInputStream(serverJksPath), jksPassword);
        mgrFact.init(serverStore, jksPassword);

        // Trust stores- Who do I trust

        // set up a trust manager so we can recognize the server
        TrustManagerFactory trustFact = TrustManagerFactory.getInstance("SunX509");
        KeyStore trustStore = KeyStore.getInstance("JKS");
        trustStore.load(new FileInputStream(trustKeystorepath), trustPassword);
        trustFact.init(trustStore);

        // create a context and set up a socket factory
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(mgrFact.getKeyManagers(), trustFact.getTrustManagers(), null);

        return sslContext;
    }

}
