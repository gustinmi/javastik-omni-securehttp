package com.gustinmi.cryptotest.httpserv;

import static com.gustinmi.cryptotest.Utils.*;
import java.io.InputStream;
import java.net.URL;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import com.gustinmi.cryptotest.certs.CertValidators;

/**
 * SSL Client with client side authentication.
 */
public class HTTPSClientExample extends SSLClientWithClientAuthTrustExample {


	public static void main(String[] args) throws Exception {

		SSLContext sslContext = createSSLContext();
		SSLSocketFactory fact = sslContext.getSocketFactory();

		// specify the URL and connection attributes
        URL url = new URL("http://" + HOST + ":" + PORT_NO);

		HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();

		connection.setSSLSocketFactory(fact);
        connection.setHostnameVerifier(new CertValidators.HostnameValidator("CN=Test CA Certificate"));

		connection.connect();

		// read the response
		InputStream in = connection.getInputStream();

		int ch = 0;
		while ((ch = in.read()) >= 0) {
			System.out.print((char) ch);
		}
	}
}