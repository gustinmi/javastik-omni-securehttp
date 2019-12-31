
// for IOException and Input/OutputStream
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
// for Socket, ServerSocket, and InetAddress
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.List;

public class TCPEchoServer {

    public static List<String> messages = new ArrayList<String>();

    private static final int BUFSIZE = 32; // Size of receive buffer

    public static void main(String[] args) {

        int servPort = 4444;

        // Create a server socket to accept client connection requests
        ServerSocket servSock;
        try {
            servSock = new ServerSocket(servPort);
            int recvMsgSize; // Size of received message
            byte[] receiveBuf = new byte[BUFSIZE]; // Receive buffer

            while (true) { // Run forever, accepting and servicing connections
                Socket clntSock = servSock.accept(); // Get client connection

                SocketAddress clientAddress = clntSock.getRemoteSocketAddress();
                System.out.println("Handling client at " + clientAddress);

                InputStream in = clntSock.getInputStream();
                OutputStream out = clntSock.getOutputStream();

                String incomingNotBaked = null;
                String incomingBaked = null;
                // Receive until client closes connection, indicated by -1 return
                while ((recvMsgSize = in.read(receiveBuf)) != -1) {
                    System.out.println("SERVER - Receiving one 32 packet : " + receiveBuf);

                    String data = new String(receiveBuf);
                    if (incomingNotBaked != null) {
                        data += incomingNotBaked;
                        incomingNotBaked = null;
                    }

                    if (data.indexOf(TCPEchoClient.TERMINATOR) > 0) {
                        incomingBaked = data.substring(0, data.indexOf(TCPEchoClient.TERMINATOR));

                        //TODO zavrzi crap 
                        incomingNotBaked = data.substring(data.indexOf(TCPEchoClient.TERMINATOR) + 1, data.length() - 1);
                    }

                    if (data.contains("BYE")) break;
                    messages.add(incomingBaked);

                }

                String msgConfirm = String.format("Recevied %s messages and closing communication", messages.size());
                out.write(msgConfirm.getBytes(), 0, msgConfirm.length());
                out.write("END".getBytes(), 0, "END".length());
                clntSock.close(); // Close the socket.  We are done with this client!
            }
            /* NOT REACHED */

        } catch (IOException e) {
            e.printStackTrace();
        }


    }
}
