import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class TCPEchoClient {

    public static String TERMINATOR = ".";
    private static final int BUFSIZE = 32;

    public static List<String> messages = new ArrayList<String>();

    public static void main(String[] args) throws IOException {

        messages.add("Danes je lep dan");
        messages.add("Vreme je oblačno");
        messages.add("Veter ne piha");
        messages.add("BYE");

        String server = "10.136.22.158"; // Server name or IP address
        // Convert argument String to bytes using the default character encoding
        byte[] data = "Client message".getBytes();

        int servPort = 4444;

        // Create socket that is connected to server on specified port
        Socket socket = new Socket(server, servPort);
        System.out.println("Connected to server...sending echo string");

        InputStream in = socket.getInputStream();
        OutputStream out = socket.getOutputStream();

        for (String msg : messages) {
            //out.write(data); // Send the encoded string to the server
            out.write(String.format("%s%s", msg, TERMINATOR).getBytes()); // Send the encoded string to the server
        }

        // Receive the same string back from the server
        //int totalBytesRcvd = 0; // Total bytes received so far
        //int bytesRcvd; // Bytes received in last read

        int recvMsgSize; // Size of received message
        byte[] receiveBuf = new byte[BUFSIZE]; // Receive buffer

        while ((recvMsgSize = in.read(receiveBuf)) != -1) {
            String response = new String(receiveBuf);
            
            System.out.println("CLIENT - Received response : " + response);
            if (response.contains("END")) break;
            
        } // data array is full

        socket.close(); // Close the socket and its streams
    }

}
