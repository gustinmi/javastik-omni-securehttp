Cryptotest
==========================

# Embedded SSL HTTP server

## Machine and JRE changes

1. Install bouncycastle provider jar to your JRE folder jre/lib/ext

1. Extract unlimited policy files to to jre/lib/security/

1. Change jre/lib/securityjava.security file. Add provider to section "List of providers and their preference orders (see above)"

`security.provider.11=org.bouncycastle.jce.provider.BouncyCastleProvider`

## Prerequisites

1. Run the CreateKeyStores.java to create all the necessary certificates

## SSL Server for testing purposes

1. Open SslServer inside com.gustinmi.ssl.
1. Run as java application. Defaults to 9020 port
1. Use openssl for connection testing
1. Connect this way `openssl s_client -connect localhost:9020 -state -debug -cert clientcert.pem -key clientkey.pem` 

