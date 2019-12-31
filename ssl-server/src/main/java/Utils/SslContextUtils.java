package Utils;

import static com.gustinmi.cryptotest.Utils.*;
import java.io.FileInputStream;
import java.security.KeyStore;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

public class SslContextUtils {

    /**
     * Create an SSL context with JKS Truststore and keystore  with identity and trust stores in place.
     * Identity : Who am I
     * Trust : who do I trust
     */
    public static SSLContext createSSLContextServer(String serverJksPath, char[] jksPassword, String trustKeystorepath, char[] trustPassword) throws Exception {

        // IDENTITY - Who am i

        // set up a key manager for our local credentials
        final KeyManagerFactory mgrFact = KeyManagerFactory.getInstance("SunX509");
        final KeyStore serverStore = KeyStore.getInstance("JKS");
        serverStore.load(new FileInputStream(serverJksPath), jksPassword);
        mgrFact.init(serverStore, jksPassword);

        // Trust stores- Who do I trust

        // set up a trust manager so we can recognize the server
        final TrustManagerFactory trustFact = TrustManagerFactory.getInstance("SunX509");
        final KeyStore trustStore = KeyStore.getInstance("JKS");
        trustStore.load(new FileInputStream(trustKeystorepath), trustPassword);
        trustFact.init(trustStore);

        // create a context and set up a socket factory
        final SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(mgrFact.getKeyManagers(), trustFact.getTrustManagers(), null);

        return sslContext;
    }

    /**
     * Create an SSL context with both identity and trust store from personal p12 PFX certiface
     */
    public static SSLContext createSSLContextClient() throws Exception {

        // set up a key manager for our local credentials
        KeyManagerFactory mgrFact = KeyManagerFactory.getInstance("SunX509");
        KeyStore clientStore = KeyStore.getInstance("PKCS12");
        clientStore.load(new FileInputStream("client.p12"), CLIENT_PASSWORD);
        mgrFact.init(clientStore, CLIENT_PASSWORD);

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
