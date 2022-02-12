package Client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    private static final String serverHost = "localhost";
    private static final int serverPort = 3000;

    public static void main(String[] args) {
        Scanner input = new Scanner(System.in);

        try (Socket socket = new Socket(serverHost, serverPort)) {
            DataOutputStream request = new DataOutputStream(socket.getOutputStream());
            DataInputStream response = new DataInputStream(socket.getInputStream());
            String message;

            System.out.println("Connection established with server.");

            while (true) {
                System.out.print("C:\t");
                message = input.nextLine();
                request.writeUTF(message);

                String reply = response.readUTF();
                System.out.println("S:\t" + reply + "\n");

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
