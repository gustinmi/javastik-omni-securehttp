Crypto Test Utilities
==========================

# Embedded SSL HTTP server

To run and test SSL HTTPS server, complete following:

## 1. Machine and JRE changes

1. Install bouncycastle provider jar to your JRE folder jre/lib/ext   (this is defacto crypto provider that suplements the defauult java provider)

1. Extract unlimited policy files to to jre/lib/. Replace existing ones. (in some countries crypo stuff is very restricted, so Java ships with minimum) 

1. Update your '''jre/lib/security/java.security''' file. Add provider to section "List of providers and their preference orders (see above)"

`security.provider.11=org.bouncycastle.jce.provider.BouncyCastleProvider`

1. Run CipherAndProviderTest.java to ensure everything works.

## 2. Creating certificates and keystores 

1. Run CreateKeyStores.java to create all the necessary certificates 
1. Install portecle.jar application (GUI application for creation and inspection of JKSs)

## SSL Server for testing purposes

1. Open SslServer inside com.gustinmi.ssl.
1. Run as java application. Defaults to 9020 port
1. Run SSLClientWithClientAuthTrustExample.java to test everything 
1. Use openssl for connection testing
1. Export private and public key from .p12 for openssl with

    openssl pkcs12 -in client.p12 -out clientcert.pem -nodes  
    openssl pkcs12 -in client.p12 -out clientkey.pem -nodes -nocerts

1. Connect this way `openssl s_client -connect localhost:9020 -state -debug -cert clientcert.pem -key clientkey.pem` 
  


## Tools 

1. portecle.jar    https://sourceforge.net/projects/portecle/files/portecle/1.9/
1. openssl         linux,
1. keytool  JDK orodje za JKS management 

## Java options

-Dhttps.protocols=TLSv1.2    Forces TLS version
-Djavax.net.debug=ssl:handshake  Used in debugging
-Djavax.net.debug=ssl:all  Used in debugging
-Djdk.tls.client.protocols=TLSv1.2  Forces protocol on client only




 