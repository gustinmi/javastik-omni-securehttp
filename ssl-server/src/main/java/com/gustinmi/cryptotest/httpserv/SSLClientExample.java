package com.gustinmi.cryptotest.httpserv;

import static com.gustinmi.cryptotest.Utils.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * Basic SSL Client - using the '!' protocol.
 */
public class SSLClientExample {

	/**
	 * Carry out the '!' protocol - client side.
	 */
	static void doProtocol(Socket cSock) throws IOException {
		OutputStream out = cSock.getOutputStream();
		InputStream in = cSock.getInputStream();

        out.write(toByteArray("World"));
		out.write('!');

		int ch = 0;
		while ((ch = in.read()) != '!') {
			System.out.print((char) ch);
		}

		System.out.println((char) ch);
	}


}
