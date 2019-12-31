import java.io.OutputStream;
import java.net.Socket;

public class VoteClientTCP {

    public static final int CANDIDATEID = 888;

    public static void main(String args[]) throws Exception {

        if (args.length != 2) { // Test for correct # of args
            throw new IllegalArgumentException("Parameter(s): <Server> <Port>");
        }

        String destAddr = args[0]; // Destination address
        int destPort = Integer.parseInt(args[1]); // Destination port

        Socket sock = new Socket(destAddr, destPort);
        OutputStream out = sock.getOutputStream();

        // Change Bin to Text for a different framing strategy
        VoteMsgCoder coder = new VoteMsgBinCoder();
        // Change Length to Delim for a different encoding strategy
        Framer framer = new LengthFramer(sock.getInputStream());

        // Create an inquiry request (2nd arg = true)
        VoteMsg msg = new VoteMsg(false, true, CANDIDATEID, 0);
        byte[] encodedMsg = coder.toWire(msg);

        // Send request
        System.out.println("Sending Inquiry (" + encodedMsg.length + " bytes): ");
        System.out.println(msg);
        framer.frameMsg(encodedMsg, out);

        // Now send a vote
        msg.setInquiry(false);
        encodedMsg = coder.toWire(msg);
        System.out.println("Sending Vote (" + encodedMsg.length + " bytes): ");
        framer.frameMsg(encodedMsg, out);

        // Receive inquiry response
        encodedMsg = framer.nextMsg();
        msg = coder.fromWire(encodedMsg);
        System.out.println("Received Response (" + encodedMsg.length + " bytes): ");
        System.out.println(msg);

        // Receive vote response
        msg = coder.fromWire(framer.nextMsg());
        System.out.println("Received Response (" + encodedMsg.length + " bytes): ");
        System.out.println(msg);

        sock.close();
    }
}
