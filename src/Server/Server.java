package Server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class Server {
    private static final Logger logger = System.getLogger(Server.class.getName());
    private static final CredsManager credManager = CredsManager.getInstance();
    private static final DecimalFormat df = new DecimalFormat("0.00");
    private static final int serverPort = 3000;

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(serverPort)) {
            logger.log(Level.INFO, "Server started.\n");

            boolean shutdown = false;
            do {
                Socket clientConn = serverSocket.accept();
                shutdown = runServer(clientConn);
            } while (!shutdown);

            logger.log(Level.INFO, "Server shut down.");
        } catch (Exception e) {
            logger.log(Level.ERROR, "Something really bad happened and the Server unexpectedly stopped.");
            e.printStackTrace();
        }
    }

    private static boolean runServer(Socket clientConn) throws IOException {
        DataInputStream request = new DataInputStream(clientConn.getInputStream());
        DataOutputStream response = new DataOutputStream(clientConn.getOutputStream());

        File solutionsFile = null;
        Writer solutionsFileWriter = null;

        try {
            while (true) {
                response.flush();
                String message = request.readUTF();
                logger.log(Level.INFO, "[{0}]: {1}", credManager.getAuthenticatedUser(), message);

                List<String> args = new ArrayList<>();
                args.addAll(Arrays.asList(message.split(" ")));

                String command = args.remove(0).toLowerCase();

                // Ensure the user is logged in before executing on a command.
                if (!command.isBlank() && !command.equals("login") && !command.equals("logout")
                        && credManager.getAuthenticatedUser().equals("")) {
                    response.writeUTF("ERROR: You must be logged in to execute this command.");
                    continue;
                }

                switch (command) {
                    case "login":
                        try {
                            String username = args.get(0);
                            String password = args.get(1);

                            if (credManager.authenticateUser(username, password)) {
                                solutionsFile = new File("solutions\\" + username + "_solutions.txt");
                                solutionsFile.getParentFile().mkdir();
                                solutionsFile.createNewFile();

                                if (solutionsFile.exists() && solutionsFile.canWrite()) {
                                    solutionsFileWriter = new FileWriter(solutionsFile, true);
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
                        solve(args, response, solutionsFileWriter);
                        break;
                    case "list":
                        list(args, response);
                        break;
                    case "shutdown":
                        logger.log(Level.INFO, "Server shutting down.");
                        response.writeUTF("200 OK");
                        return true;
                    case "logout":
                        credManager.logoutUser();
                        response.writeUTF("200 OK");
                        return false;
                    default:
                        response.writeUTF("300 invalid command");
                        break;
                }
            }
        } catch (EOFException | SocketException e) {
            logger.log(Level.WARNING, "The client's connection has dropped.");
            return false;
        } finally {
            if (solutionsFileWriter != null) {
                solutionsFileWriter.close();
            }

            clientConn.close();
        }
    }

    private static void solve(List<String> args, DataOutputStream response, Writer writer) throws IOException {
        boolean isRectangle = args.contains("-r");
        boolean isCircle = args.contains("-c");

        Double first = null;
        Double second = null;
        Double area = null;
        Double perimeter = null;

        if ((!isRectangle && !isCircle) || (isRectangle && isCircle)) {
            response.writeUTF("ERROR: Invalid operation(s) specified");
            return;
        } else {
            args.remove(0);
        }

        if (args.isEmpty()) {
            String result = "";
            if (isRectangle) {
                result = "ERROR: No sides found";
            } else if (isCircle) {
                result = "ERROR: No radius found";
            }

            writer.append(result);
            writer.append("\n");
            writer.flush();
            
            response.writeUTF(result);
            return;
        }

        try {
            first = Double.valueOf(args.get(0));

            if (args.size() >= 2) {
                second = Double.valueOf(args.get(1));
            } else {
                second = first;
            }
        } catch (NumberFormatException e) {
            response.writeUTF("301 message format error");
            return;
        }

        // solve.
        if (isRectangle) {
            area = first * second;
            perimeter = 2 * (first + second);

            String solution = MessageFormat.format(
                    "Rectangle''s perimeter is {0} and area is {1}",
                    df.format(perimeter), df.format(area));

            writer.append("sides " + first + " " + second + ":\t" + solution);
            writer.append("\n");
            writer.flush();

            response.writeUTF(solution);
            return;
        } else if (isCircle) {
            area = Math.PI * Math.pow(first, 2);
            perimeter = 2 * Math.PI * first;

            String solution = MessageFormat.format(
                    "Circle''s circumference is {0} and area is {1}",
                    df.format(perimeter), df.format(area));

            writer.append("radius " + first + ":\t" + solution);
            writer.append("\n");
            writer.flush();

            response.writeUTF(solution);
            return;
        }

        response.writeUTF("ERROR: The operation failed spectacularly, and it's unknown why! :(");
    }

    private static void list(List<String> args, DataOutputStream response) throws IOException {
        String currentUser = credManager.getAuthenticatedUser();
        StringBuilder result = new StringBuilder("\n");

        if (args.contains("-all")) {
            if (!currentUser.equalsIgnoreCase("root")) {
                response.writeUTF("FAILURE: This method is only accessible to the root user");
                return;
            }

            for (String username : credManager.getAllUsernames()) {
                result.append(MessageFormat.format("\t{0}\n", username));
                result.append(getInteractions(username));
            }

            response.writeUTF(result.toString());
            return;
        }

        result.append(getInteractions(currentUser));
        response.writeUTF(result.toString());
    }

    private static String getInteractions(String username) throws FileNotFoundException {
        StringBuilder result = new StringBuilder();
        File userSolutionsFile = new File("solutions\\" + username + "_solutions.txt");

        if (userSolutionsFile.exists()) {
            try (Scanner reader = new Scanner(userSolutionsFile)) {
                if (!reader.hasNextLine()) {
                    result.append("\t\tNo interactions yet\n");
                    return result.toString();
                }

                while (reader.hasNextLine()) {
                    result.append(MessageFormat.format("\t\t{0}\n", reader.nextLine()));
                }
            }
        } else {
            result.append("\t\tNo interactions yet\n");
        }

        return result.toString();
    }
}
