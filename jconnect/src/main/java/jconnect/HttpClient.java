package jconnect;

import java.io.*;
import java.net.*;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.ssl.*;
import jconnect.WebAr.*;

/**
 * HTTP client is generic interface to HTTP server and services.
 * REST interface, form post and multipart as well
 * Could interact with SSL enabled services or HTTP with or without authentication
 * Supports proxy
 * Main components are: url parser and builder, HTTP protocol handler, secure SSL/TLS component
 * @author Mitja Gustin gustinmi [at] gmail [dot] com   
 */
public class HttpClient {

    public static final Logger Log = Logger.getLogger(HttpClient.class.getCanonicalName());

    /** Private CA certificate */
    public static class CaCertPem {
        public static String certPem = "___PLACEHOLDER_FOR_YOUR_PRIVATE_CA_CERTIFICATE___";
    }

    private static int MAX_RETRIES = 5; // Num of requests before error is reported

    public static String caBundleDefault = CaCertPem.certPem;

    public static Boolean verboseDefault = false;

    private final boolean verbose;
    private final boolean useProxy;
    private final InetSocketAddress proxyAddress;
    private final TimeoutDefs timeoutDefs;

    private final SSLSocketFactory trustedSocketFactory;
    private final HostnameVerifier hostnameVerifier;

    /** Build the client with instance level settings. The request are done on basis of these settings 
     * @param httpsCheck Optional HTTPS security level
     * @param caBundle Optional Private CA certificate bundle
     * @param timeoutDefs Optional timeouts in milliseconds
     * @param verbose Optional verbose mode 
     * @param proxy Optional proxy
     * @param ksLoc Optional location of keystore
     * @param ksPassword Password mfor keystore
     */
    public HttpClient(HTTPS_OPTS httpsCheck, String caBundle, TimeoutDefs timeoutDefs, Boolean verbose, InetSocketAddress proxy, String ksLoc, String ksPassword) throws IllegalStateException, FileNotFoundException {

        this.verbose = verbose == null ? false : verbose;
        this.timeoutDefs = timeoutDefs;
        this.useProxy = proxy == null ? false : true;
        this.proxyAddress = proxy;
        switch (httpsCheck) {

            case HTTPS_BASIC: { /* create insecure all trusting keystore and trustore */
                // use HTTP basic auth or without any verification
                trustedSocketFactory = getSocketFactoryInsecure();
                // turn off hostname verification
                hostnameVerifier = getHostnameVerifierInsecure();
                break;
            }
            case HTTPS_DEVELOPMENT: { /* creates keystores on the fly with fixed hardcoded cert */

                // Development boundle for creating certs and JKS keystore 
                final String caBundleDefault = CaCertPem.certPem;

                // with private CA bundle verification
                trustedSocketFactory = getSocketFactoryFromCert(caBundleDefault);
                // turn off hostname verification
                hostnameVerifier = getHostnameVerifierInsecure();
                break;
            }
            case HTTPS_PRIVATE_CA: { /*  creates keystores from provided boundle */
                if (caBundle == null) throw new IllegalArgumentException("Option HTTPS_PRIVATE_CA expects caBundle");
                // with private CA bundle verification
                trustedSocketFactory = getSocketFactoryFromCert(caBundle);
                // regular hostname verification
                hostnameVerifier = HttpsURLConnection.getDefaultHostnameVerifier();
                break;
            }
            case HTTPS_PROVIDED_CA: { /* use keystore in physical location (embedded into jar or file storage) */

                if (ksLoc == null) throw new IllegalArgumentException("Option HTTPS_PROVIDED_CA expects ksLoc");

                // use provided JKS keystore
                trustedSocketFactory = getSocketFactoryFromKeyStore(ksLoc, ksPassword); // todo
                //regular hostname verification
                hostnameVerifier = HttpsURLConnection.getDefaultHostnameVerifier();
                break;
            }
            case HTTPS_DEFAULT_PUBLIC_CA: { /* use java default keystore and truststore */
                // regular public CA verification
                trustedSocketFactory = HttpsURLConnection.getDefaultSSLSocketFactory();
                // regular hostname verification
                hostnameVerifier = HttpsURLConnection.getDefaultHostnameVerifier();
                break;
            }

            default:
                throw new IllegalStateException("Not supported HTTPS_OPTS option " + httpsCheck.name());

        }

    }

    /** Overload for request without body */
    public Response request(String method, final URL url) throws Exception {
        final HttpRequestType httpMethod = HttpRequestType.valueOf(method);
        if (httpMethod.equals(HttpRequestType.GET) || httpMethod.equals(HttpRequestType.HEAD) || httpMethod.equals(HttpRequestType.OPTIONS)) {
            return this.request(method, url, null, null, null);
        }
        throw new IllegalStateException("Cannot issue " + method + " with no data. Use request method with data");
    }

    /** Custom REST request. For URL use {@link UrlBuilder} */
    public Response request(String method, final URL url, String rawData, KeyValueData[] formData, UploadData[] files) throws Exception {
        HttpURLConnection conn = null;
        Exception lastException = null;
        Response response = null;

        final HttpRequestType httpMethod = HttpRequestType.valueOf(method);

        for (int i = MAX_RETRIES - 1; i >= 0; --i) {
            try {
                // initialize connection
                conn = getConnection(url);

                response = HttpProtocol.makeRequest(this.timeoutDefs, httpMethod, conn, rawData, formData, files);

                // no exception
                lastException = null;
                break;

            } catch (Exception e) {
                Log.log(Level.SEVERE, "Exception (retrying " + i + "): " + e.toString());
                lastException = e;
            }
        }
        if (lastException != null) { // raise last exception
            throw lastException;
        }

        // verify status code
        if (this.verbose) Log.info(String.format("  = %d: %s", response.getStatusCode(), response.getContent()));
        if (response.getStatusCode() < 200 || response.getStatusCode() >= 300)
            throw new ProtocolException(String.format("Unsuccessful HTTP status code: %s from %s %s", response.getStatusCode(), method, url.toString()));
        return response;
    }

    /** Helper function for SSL/TLS sockets trusting only certificates from a given private CA. */
    public static SSLSocketFactory getSocketFactoryFromCert(String certPem) {
        try {
            // parse CA certificate in DER format from PEM
            String[] certTokens = certPem.split("-----BEGIN CERTIFICATE-----");
            certTokens = certTokens[1].split("-----END CERTIFICATE-----");
            byte[] certDer = jconnect.Base64.decode(certTokens[0]);

            // generate Certificate from DER format
            CertificateFactory factory = CertificateFactory.getInstance("X.509");
            Certificate caCert = factory.generateCertificate(new ByteArrayInputStream(certDer));

            // create a KeyStore containing our trusted CAs
            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            keyStore.load(null, null);
            keyStore.setCertificateEntry("ca", caCert);

            // create a TrustManager that trusts the CAs in our KeyStore
            String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
            tmf.init(keyStore);

            // create an SSLContext that uses our TrustManager
            SSLContext context = SSLContext.getInstance("TLS");
            context.init(null, tmf.getTrustManagers(), null);
            return context.getSocketFactory();

        } catch (Exception e) {
            Log.log(Level.SEVERE, "Exception: " + e.toString(), e);
        }
        return null;
    }

    /** Helper function for SSL/TLS sockets that insecurely trusts all certificates. */
    public static SSLSocketFactory getSocketFactoryInsecure() {
        try {
            // create a TrustManager that trusts all certificates
            TrustManager[] trustAllCerts = new TrustManager[] { new InsecureX509TrustManager() };

            // create an SSLContext that uses our TrustManager
            SSLContext context = SSLContext.getInstance("TLS");
            context.init(null, trustAllCerts, null);
            return context.getSocketFactory();

        } catch (Exception e) {
            Log.log(Level.SEVERE, "Exception: " + e.toString(), e);
        }
        return null;
    }

    private static SSLSocketFactory getSocketFactoryFromKeyStore(String ksLoc, String ksPassword) throws IllegalStateException, FileNotFoundException {

        Log.finest("Configuring keystore");

        final boolean isFile = new File(ksLoc).exists();

        final char[] password = ksPassword.toCharArray();
        final InputStream is;
        if (isFile) { // try load from file
            is = new FileInputStream(ksLoc);
        } else { // try load from EAR/WAR/JAR/SAR
            is = HttpClient.class.getClassLoader().getResourceAsStream(ksLoc);
        }

        if (is == null) { throw new IllegalStateException("Cannot find keystore on location: " + ksLoc); }

        // try to open keystrore with password
        final KeyStore keyStore;
        try {
            keyStore = KeyStore.getInstance("JKS");
            keyStore.load(is, password);
        } catch (KeyStoreException e) {
            final String message = "Key store failed while opening it: " + e.toString();
            Log.severe(message + e);
            throw new IllegalStateException(message, e);
        } catch (CertificateException e) {
            final String message = "A problem with a certificate: " + e.toString();
            Log.severe(message + e);
            throw new IllegalStateException(message, e);
        } catch (NoSuchAlgorithmException e) {
            final String message = "A cryptographic algorithm is not available: " + e.toString();
            Log.severe(message + e);
            throw new IllegalStateException(message, e);
        } catch (IOException e) {
            final String message = "Failed to open the client certificate file: " + e.toString();
            Log.severe(message + e);
            throw new IllegalStateException(message, e);
        }
        finally {
            try {
                is.close();
            } catch (IOException e) {} // close keystore forcefully
        }

        Log.finest("Keystore is ready.");

        try {

            Log.finest("Preparing SSL socket factory.");

            final KeyManagerFactory keyFact = KeyManagerFactory.getInstance("SunX509");
            keyFact.init(keyStore, ksPassword.toCharArray());

            final TrustManagerFactory trustFact = TrustManagerFactory.getInstance("SunX509");
            trustFact.init(keyStore);

            final SSLContext c = SSLContext.getInstance("TLS");
            c.init(keyFact.getKeyManagers(), trustFact.getTrustManagers(), null);

            Log.finest("SSL socket factory preparation success");

            return c.getSocketFactory();
        } catch (GeneralSecurityException e) {
            Log.log(Level.SEVERE, "Error while preparing SSL socket factory", e);
        }

        return null;
    }

    /** Ignore hostname in certificate check. */
    public static HostnameVerifier getHostnameVerifierInsecure() {
        return new HostnameVerifier() {
            @Override
            public boolean verify(String hostname, SSLSession session) {
                return true;
            }
        };
    }

    protected HttpURLConnection getConnection(final URL url) {
        try {
            final HttpURLConnection connection;
            if (useProxy) {

                if (verbose) Log.info("PROXY Connecting to " + url);
                final Proxy proxy = new Proxy(Proxy.Type.HTTP, this.proxyAddress);
                connection = (HttpURLConnection) url.openConnection(proxy);
                if (verbose) Log.info("Connected.");

            } else {

                if (verbose) Log.info("NOPROXY Connecting to " + url);
                connection = (HttpURLConnection) url.openConnection(Proxy.NO_PROXY);
                if (verbose) Log.info("Connected.");

            }

            if (connection instanceof HttpsURLConnection) {
                final HttpsURLConnection c = (HttpsURLConnection) connection;
                if (verbose) Log.info("Setting SSL options on connection");
                c.setSSLSocketFactory(this.trustedSocketFactory);
                c.setHostnameVerifier(this.hostnameVerifier);
            }

            if (connection != null) return connection;
        } catch (Exception e) {
            Log.log(Level.SEVERE, String.format("Error %s while preparing %s connectiona", e.getMessage(), url), e);
        }

        throw new IllegalStateException("Could not connect to varnostna shema!");

    }

    /**
     * This class allow any X509 certificates to be used to authenticate the
     * remote side of a secure socket, including self-signed certificates.
     *
     * @author    Francis Labrie
     */
    public static class InsecureX509TrustManager implements X509TrustManager {

        /**
         * Empty array of certificate authority certificates.
         */
        private static final X509Certificate[] _AcceptedIssuers = new X509Certificate[] {};

        /**
         * Always trust for client SSL chain peer certificate
         * chain with any authType authentication types.
         *
         * @param chain           the peer certificate chain.
         * @param authType        the authentication type based on the client
         * certificate.
         */
        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType) {} // checkClientTrusted

        /**
         * Always trust for server SSL chain peer certificate
         * chain with any authType exchange algorithm types.
         *
         * @param chain           the peer certificate chain.
         * @param authType        the key exchange algorithm used.
         */
        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType) {} // checkServerTrusted

        /**
         * Return an empty array of certificate authority certificates which
         * are trusted for authenticating peers.
         *
         * @return                a empty array of issuer certificates.
         */
        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return (_AcceptedIssuers);
        } // getAcceptedIssuers
    } // FakeX509

}