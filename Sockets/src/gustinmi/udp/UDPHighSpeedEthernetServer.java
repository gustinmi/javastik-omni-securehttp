package gustinmi.udp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.SocketTimeoutException;

public class UDPHighSpeedEthernetServer {

    static public int destport = 5432;
    static public int bufsize = 512;
    static public final int timeout = 15000; // time in milliseconds

    static public void main(String args[]) {

        DatagramSocket dgSocket;               // UDP uses DatagramSockets

        try {
            dgSocket = new DatagramSocket(destport);
        } catch (SocketException se) {
            System.err.println("cannot create socket with port " + destport);
            return;
        }
        try {
            dgSocket.setSoTimeout(timeout);       // set timeout in milliseconds
        } catch (SocketException se) {
            System.err.println("socket exception: timeout not set!");
        }

        byte[] requestBuffer = new byte[bufsize];
        byte[] respBuffer = new byte[bufsize];

        // create DatagramPacket object for receiving data:
        DatagramPacket request = new DatagramPacket(requestBuffer, bufsize);
        DatagramPacket response = new DatagramPacket(respBuffer, bufsize);

        while (true) { // read loop

            try {

                // receive / wait
                request.setLength(bufsize);  // max received packet size
                dgSocket.receive(request);          // blocks >> the actual receive operation
                System.err.println("message from <" + request.getAddress().getHostAddress() + "," + request.getPort() + ">");

                // send back

                dgSocket.send(response); // Send the same packet back to client

                response.setLength(bufsize); // Reset length to avoid shrinking buffer

            } catch (SocketTimeoutException ste) {    // receive() timed out
                System.err.println("Response timed out!");
                continue;
            } catch (IOException ioe) {                // should never happen!
                System.err.println("Bad receive");
                break;
            }

            // get data always returns original bufer (no internal offset)
            String str = new String(request.getData(), 0, request.getLength());
            System.out.print(str);        // newline must be part of str
        }

        dgSocket.close();
    } // end of main

}
