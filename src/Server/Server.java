package Server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.List;

public class Server {
    private static final Logger LOGGER = System.getLogger(Server.class.getName());
    private static final CredsManager CREDS_MANAGER = CredsManager.getInstance();
    private static final int SERVER_PORT = 3000;

    private static void initializeSockets() {
        try (ServerSocket serverSocket = new ServerSocket(SERVER_PORT)) {
            LOGGER.log(Level.INFO, "Server started.\n");

            Socket clientConn = serverSocket.accept();
            runServer(clientConn);

            LOGGER.log(Level.INFO, "Server shut down.");
        } catch (Exception e) {
            // Some error handling here...
            e.printStackTrace();
        }
    }

    private static void runServer(Socket clientConn) throws IOException {
        DataInputStream request = new DataInputStream(clientConn.getInputStream());
        DataOutputStream response = new DataOutputStream(clientConn.getOutputStream());

        while (true) {
            String message = request.readUTF();
            List<String> commands = Arrays.asList(message.split(" "));
            String command = commands.get(0).toLowerCase();

            // Ensure the user is logged in before executing on a command.
            if (!command.equals("login") && !command.equals("logout")
                    && CREDS_MANAGER.getAuthenticatedUser().equals("")) {
                response.writeUTF("ERROR: You must be logged in to execute this command.");
                continue;
            }

            switch (command) {
                case "login":
                    try {
                        String username = commands.get(1);
                        String password = commands.get(2);

                        if (CREDS_MANAGER.authenticateUser(username, password)) {
                            response.writeUTF("SUCCESS");
                        } else {
                            response.writeUTF("FAILURE: Please provide a valid username and password");
                        }
                    } catch (IndexOutOfBoundsException e) {
                        response.writeUTF("301 message format error");
                    }
                    break;
                case "solve":
                    boolean isRectangle = commands.contains("-r");
                    boolean isCircle = commands.contains("-c");

                    if ((!isRectangle && !isCircle) || (isRectangle && isCircle)) {
                        response.writeUTF("ERROR: Invalid operation(s) specified");
                        break;
                    }

                    if (commands.size() < 3) {
                        if (isRectangle) {
                            response.writeUTF("ERROR: No sides found");
                        } else if (isCircle) {
                            response.writeUTF("ERROR: No radius found");
                        }
                        break;
                    }

                    Double a = null;
                    Double b = null;
                    try {
                        if (commands.size() >= 4) {
                            b = Double.valueOf(commands.get(4));
                        }
                        if (commands.size() >= 3) {
                            a = Double.valueOf(commands.get(3));
                        }
                    } catch (NumberFormatException e) {
                        response.writeUTF("ERROR: Invalid values provided");
                    }

                    // solve.
                    double area;
                    double perimeter;
                    if (isRectangle) {
                        if (b != null) {
                            area = a * b;
                            perimeter = 2 * (a + b);
                        }

                        response.writeUTF("Placeholder");
                        break;
                    } else if (isCircle) {
                        area = Math.PI * Math.pow(a, 2);
                        perimeter = 2 * Math.PI * a;

                        response.writeUTF("Placeholder");
                        break;
                    }

                    response.writeUTF("ERROR");
                    break;
                case "list":
                    String authedUser = CREDS_MANAGER.getAuthenticatedUser();

                    if (commands.contains("-all") && !authedUser.equalsIgnoreCase("root")) {
                        response.writeUTF("FAILURE: This method is only accessible to the root user");
                        break;
                    }

                    response.writeUTF("Placeholder.");
                    break;
                case "shutdown":
                    clientConn.close();
                    LOGGER.log(Level.INFO, "Server shutting down.");
                    response.writeUTF("200 OK");
                    return;
                case "logout":
                    CREDS_MANAGER.logoutUser();
                    response.writeUTF("200 OK");
                    break;
                default:
                    response.writeUTF("300 invalid command");
                    break;
            }
        }
    }

    public static void main(String[] args) {
        initializeSockets();
    }
}
