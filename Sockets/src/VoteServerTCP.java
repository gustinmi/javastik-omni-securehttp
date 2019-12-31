import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class VoteServerTCP {

    public static void main(String args[]) throws Exception {

        if (args.length != 1) { // Test for correct # of args
            throw new IllegalArgumentException("Parameter(s): <Port>");
        }

        int port = Integer.parseInt(args[0]); // Receiving Port

        ServerSocket servSock = new ServerSocket(port);
        // Change Bin to Text on both client and server for different encoding
        VoteMsgCoder coder = new VoteMsgBinCoder();
        VoteService service = new VoteService();

        while (true) {
            Socket clntSock = servSock.accept();
            System.out.println("Handling client at " + clntSock.getRemoteSocketAddress());
            // Change Length to Delim for a different framing strategy
            Framer framer = new LengthFramer(clntSock.getInputStream());
            try {
                byte[] req;
                while ((req = framer.nextMsg()) != null) {
                    System.out.println("Received message (" + req.length + " bytes)");
                    VoteMsg responseMsg = service.handleRequest(coder.fromWire(req));
                    framer.frameMsg(coder.toWire(responseMsg), clntSock.getOutputStream());
                }
            } catch (IOException ioe) {
                System.err.println("Error handling client: " + ioe.getMessage());
            }
            finally {
                System.out.println("Closing connection");
                clntSock.close();
            }
        }
    }
}
