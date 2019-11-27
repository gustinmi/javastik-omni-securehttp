package com.gustinmi.ssltester;

import static com.gustinmi.cryptotest.Flags.*;
import static com.gustinmi.cryptotest.Utils.*;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.gustinmi.cryptotest.Utils;

public class SimpleSocketServer {

    public static final Logger log = Utils.loggerForThisClass();

    public static void main(String[] args) throws Exception {

        System.out.println("Serving client on " + PORT_NO);

        @SuppressWarnings("resource")
        final ServerSocket serverSocket = new ServerSocket(PORT_NO);

        for (;;) { // run forever

            final Socket clntSock = serverSocket.accept(); // Get client connection
            final SocketAddress clientAddress = clntSock.getRemoteSocketAddress();
            if (SYSOUT_ENABLED) System.out.printf("Handling client at %s \n", clientAddress);

            try {
                HttpProtocol.doProtocolServer(clntSock);
            } catch (IOException e) {
                log.log(Level.SEVERE, e.getMessage(), e);
                continue; // continue serving, do not stop if faulty client connects
            }

        }


    }

}
