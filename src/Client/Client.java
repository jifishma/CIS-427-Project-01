package Client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    private static final Logger LOGGER = System.getLogger(Client.class.getName());
    private static final int SERVER_PORT = 3000;

    public static void main(String[] args) {

        DataOutputStream request;
        DataInputStream response;
        Scanner input = new Scanner(System.in);

        try (Socket socket = new Socket("localhost", SERVER_PORT)) {
            request = new DataOutputStream(socket.getOutputStream());
            response = new DataInputStream(socket.getInputStream());
            String message;

            while (true) {
                System.out.print("Send command to server:\t");
                message = input.nextLine();
                request.writeUTF(message);
                if (message.equalsIgnoreCase("quit")) {
                    break;
                }

                message = response.readUTF();
                System.out.println("Server says: " + message);
            }

            input.close();
        } catch (IOException ex) {
            input.close();
            ex.printStackTrace();
        } // end try-catch

    }// end main
}
