import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class OracleExample {

    public static class Client {
        
        public static void main(String[] args) throws IOException {
            
            String hostName = "10.136.22.158";
            int portNumber = 4444;
    
            try(Socket echoSocket = new Socket(hostName, portNumber);
                    PrintWriter outStream = new PrintWriter(echoSocket.getOutputStream(), true); BufferedReader inStream = new BufferedReader(new InputStreamReader(echoSocket.getInputStream()));
                BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in)))
            {
                
                outStream.println("Danes je lep dan.");
                System.out.println("echo: " + inStream.readLine());
                
            }
            
            
            
            
        }

    }

}
