package Client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 3000;
    public static void main(String[] args) {
        Scanner input = new Scanner(System.in);

        try (Socket socket = new Socket(SERVER_HOST, SERVER_PORT)) {
            DataOutputStream request = new DataOutputStream(socket.getOutputStream());
            DataInputStream response = new DataInputStream(socket.getInputStream());
            String message;

            while (true) {
                System.out.print("Send command to server:\t");
                message = input.nextLine();
                request.writeUTF(message);

                String reply = response.readUTF();
                System.out.println("Server says: " + reply);

                if ((message.equalsIgnoreCase("logout") || message.equalsIgnoreCase("shutdown"))
                        && reply.equalsIgnoreCase("200 OK")) {
                    break;
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            input.close();
        }
    }
}
