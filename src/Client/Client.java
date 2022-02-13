package Client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.net.Socket;
import java.text.MessageFormat;
import java.util.Scanner;

public class Client {
    private static final Logger LOGGER = System.getLogger(Client.class.getName());
    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 3000;

    public static void main(String[] args) {
        // Initialize the user's input first!
        Scanner input = new Scanner(System.in);

        // Then we attempt to establish a connection to the server using a
        // try-with-resources. This ensures that the socket is closed after
        // the block exist.
        try (Socket socket = new Socket(SERVER_HOST, SERVER_PORT)) {
            // Once we connect, establish the Server Data Streams
            DataOutputStream request = new DataOutputStream(socket.getOutputStream());
            DataInputStream response = new DataInputStream(socket.getInputStream());
            String message;

            // And alert the user of which Server they connected to.
            System.out.println(MessageFormat.format("Conection established with {0}", socket.getInetAddress()));

            // Then we enter a message loop that continues until the user logs out of or
            // shuts down the server
            while (true) {
                System.out.print("C:\t");
                message = input.nextLine();
                // Submit a request or command to the Server
                request.writeUTF(message);

                // Collect the Server's reply back and print it out for the user
                String reply = response.readUTF();
                System.out.println("S:\t" + reply + "\n");

                // If the user asked to log out or shut down,check that the Server responded
                // with a '200 OK' and exit the message loop
                if ((message.equalsIgnoreCase("logout") || message.equalsIgnoreCase("shutdown"))
                        && reply.equalsIgnoreCase("200 OK")) {
                    break;
                }
            }

            // Then close the Data Streams
            request.close();
            response.close();
        } catch (Exception ex) {
            LOGGER.log(Level.ERROR, "Something really bad happened and the Client unexpectedly stopped.");
            ex.printStackTrace();
        } finally {
            // Ensure we close the input stream as well
            input.close();
        }
    }
}
