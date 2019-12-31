import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class UDPEchoClientTimeout {

    private static final int TIMEOUT = 3000; // Resend timeout (milliseconds)
    private static final int MAXTRIES = 5; // Maximum retransmissions

    public static void main(String[] args) throws IOException, InterruptedException {

        InetAddress serverAddress = InetAddress.getByName("10.136.22.158"); // Server address


        int servPort = 5432;

        DatagramSocket socket = new DatagramSocket();

        socket.setSoTimeout(TIMEOUT); // Maximum receive blocking time (milliseconds)

        // Sending packet
        DatagramPacket sendPacket;
        // Receiving packet
        DatagramPacket receivePacket;

        for (int i = 0; i < 1000; i++) {

            int tries = 0; // Packets may be lost, so we have to keep trying
            boolean receivedResponse = false;
            do {

                // Convert the argument String to bytes using the default encoding
                byte[] bytesToSend = String.format("hello%s", i).getBytes();

                // Sending packet
                sendPacket = new DatagramPacket(bytesToSend, bytesToSend.length, serverAddress, servPort);

                // Receiving packet
                receivePacket = new DatagramPacket(new byte[bytesToSend.length], bytesToSend.length);

                socket.send(sendPacket); // Send the echo string
                try {

                    socket.receive(receivePacket); // Attempt echo reply reception

                    if (!receivePacket.getAddress().equals(serverAddress)) {// Check source
                        throw new IOException("Received packet from an unknown source");
                    }
                    receivedResponse = true;
                } catch (InterruptedIOException e) { // We did not get anything
                    tries += 1;
                    System.out.println("Timed out, " + (MAXTRIES - tries) + " more tries...");
                }
            } while ((!receivedResponse) && (tries < MAXTRIES));

            if (receivedResponse) {
                System.out.println("Received: " + new String(receivePacket.getData()));
            } else {
                System.out.println("No response -- giving up.");
            }

            Thread.sleep(200);

        }

        socket.close();
    }
}
