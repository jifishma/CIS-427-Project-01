package Server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class Server {
    private static final Logger LOGGER = System.getLogger(Server.class.getName());
    private static final CredsManager CREDS_MANAGER = CredsManager.getInstance();
    private static final int SERVER_PORT = 3000;

    private static void initializeSockets() {
        try (ServerSocket serverSocket = new ServerSocket(SERVER_PORT)) {
            LOGGER.log(Level.INFO, "Server started.\n");

            boolean shutdown = false;
            do {
                Socket clientConn = serverSocket.accept();
                shutdown = runServer(clientConn);
            } while (!shutdown);

            LOGGER.log(Level.INFO, "Server shut down.");
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, "Something really bad happened and the Server unexpectedly stopped.");
            e.printStackTrace();
        }
    }

    private static boolean runServer(Socket clientConn) throws IOException {
        DataInputStream request = new DataInputStream(clientConn.getInputStream());
        DataOutputStream response = new DataOutputStream(clientConn.getOutputStream());

        File solutionsFile = null;
        Scanner solutionsFileReader = null;

        try {
            while (true) {
                response.flush();
                String message = request.readUTF();
                LOGGER.log(Level.TRACE, "[{0}]: {1}", CREDS_MANAGER.getAuthenticatedUser(), message);

                List<String> args = new ArrayList<>();
                args.addAll(Arrays.asList(message.split(" ")));

                String command = args.remove(0).toLowerCase();

                // Ensure the user is logged in before executing on a command.
                if (!command.isBlank() && !command.equals("login") && !command.equals("logout")
                        && CREDS_MANAGER.getAuthenticatedUser().equals("")) {
                    response.writeUTF("ERROR: You must be logged in to execute this command.");
                    continue;
                }

                switch (command) {
                    case "login":
                        try {
                            String username = args.get(0);
                            String password = args.get(1);

                            if (CREDS_MANAGER.authenticateUser(username, password)) {
                                solutionsFile = new File("solutions/" + username + "_solutions.txt");

                                if (solutionsFile.getParentFile().mkdir() && solutionsFile.createNewFile()) {
                                    solutionsFileReader = new Scanner(solutionsFile);
                                }

                                response.writeUTF("SUCCESS");
                            } else {
                                response.writeUTF("FAILURE: Please provide a valid username and password");
                            }
                        } catch (IndexOutOfBoundsException e) {
                            response.writeUTF("301 message format error");
                        }
                        break;
                    case "solve":
                        boolean isRectangle = args.contains("-r");
                        boolean isCircle = args.contains("-c");

                        if ((!isRectangle && !isCircle) || (isRectangle && isCircle)) {
                            response.writeUTF("ERROR: Invalid operation(s) specified");
                            break;
                        } else {
                            args.remove(0);
                        }

                        if (args.isEmpty()) {
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
                            a = Double.valueOf(args.get(0));

                            if (args.size() >= 2) {
                                b = Double.valueOf(args.get(1));
                            }
                        } catch (NumberFormatException e) {
                            response.writeUTF("301 message format error");
                            break;
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

                        if (args.contains("-all") && !authedUser.equalsIgnoreCase("root")) {
                            response.writeUTF("FAILURE: This method is only accessible to the root user");
                            break;
                        }

                        response.writeUTF("Placeholder.");
                        break;
                    case "shutdown":
                        LOGGER.log(Level.INFO, "Server shutting down.");
                        response.writeUTF("200 OK");
                        return true;
                    case "logout":
                        CREDS_MANAGER.logoutUser();
                        response.writeUTF("200 OK");
                        return false;
                    default:
                        response.writeUTF("300 invalid command");
                        break;
                }
            }
        } finally {
            if (solutionsFileReader != null) {
                solutionsFileReader.close();
            }

            clientConn.close();
        }
    }

    public static void main(String[] args) {
        initializeSockets();
    }
}
