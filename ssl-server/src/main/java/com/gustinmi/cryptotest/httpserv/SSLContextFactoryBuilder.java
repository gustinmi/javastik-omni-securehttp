package com.gustinmi.cryptotest.httpserv;

import static com.gustinmi.cryptotest.Utils.*;
import java.io.FileInputStream;
import java.security.KeyStore;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

/** Build Context factory (loads and sets TrustStore, Keystore)
 * @author gustin
 *
 */
public class SSLContextFactoryBuilder {


    public static SSLContext createServerSSLContext() throws Exception {

        // IDENTITY - Who am i

        // set up a key manager for our local credentials
        KeyManagerFactory mgrFact = KeyManagerFactory.getInstance("SunX509");
        KeyStore serverStore = KeyStore.getInstance("JKS");
        serverStore.load(new FileInputStream("server.jks"), SERVER_PASSWORD);
        mgrFact.init(serverStore, SERVER_PASSWORD);

        // Trust stores- Who do I trust

        // set up a trust manager so we can recognize the server
        TrustManagerFactory trustFact = TrustManagerFactory.getInstance("SunX509");
        KeyStore trustStore = KeyStore.getInstance("JKS");
        trustStore.load(new FileInputStream("trustStore.jks"), TRUST_STORE_PASSWORD);
        trustFact.init(trustStore);

        // create a context and set up a socket factory
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(mgrFact.getKeyManagers(), trustFact.getTrustManagers(), null);

        return sslContext;
    }

}
