import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class UDPEchoServer {

    private static final int ECHOMAX = 255; // Maximum size of echo datagram

    static public int servPort = 5432;

    public static void main(String[] args) throws IOException {

        DatagramSocket socket = null;
        try {
            socket = new DatagramSocket(servPort);
            DatagramPacket packet = new DatagramPacket(new byte[ECHOMAX], ECHOMAX);

            while (true) { // Run forever, receiving and echoing datagrams
                socket.receive(packet); // Receive packet from client
                System.out.println("Handling client at " + packet.getAddress().getHostAddress() + " on port " + packet.getPort());
                byte[] dataRecevied = packet.getData();

                System.out.println("  >> received " + new String(dataRecevied));

                socket.send(packet); // Send the same packet back to client
                packet.setLength(ECHOMAX); // always Reset length to avoid shrinking buffer (to fit all answers). Actual response may differ in size
            }
            /* NOT REACHED */
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        
    }
}
