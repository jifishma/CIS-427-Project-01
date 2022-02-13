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
        Scanner input = new Scanner(System.in);

        try (Socket socket = new Socket(SERVER_HOST, SERVER_PORT)) {
            DataOutputStream request = new DataOutputStream(socket.getOutputStream());
            DataInputStream response = new DataInputStream(socket.getInputStream());
            String message;

            System.out.println(MessageFormat.format("Conection established with {0}", socket.getInetAddress()));

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

            request.close();
            response.close();
        } catch (Exception ex) {
            LOGGER.log(Level.ERROR, "Something really bad happened and the Client unexpectedly stopped.");
            ex.printStackTrace();
        } finally {
            input.close();
        }
    }
}
