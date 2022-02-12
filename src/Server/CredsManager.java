package Server;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.InvalidPathException;
import java.security.InvalidParameterException;
import java.util.HashMap;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CredsManager {

    private static final HashMap<String, String> credentials = new HashMap<>();
    private static final Pattern r = Pattern.compile("(?<username>^\\w*)\\s+(?<password>\\w*$)");
    private static CredsManager instance = null;

    private String authenticatedUser = "";

    public static CredsManager getInstance() {
        if (instance == null)
            instance = new CredsManager();

        return instance;
    }

    private CredsManager() {
        File credsFile = new File("logins.txt");

        if (!credsFile.exists() || !credsFile.canRead()) {
            // File doesn't exist.
            throw new InvalidPathException(credsFile.getAbsolutePath(), "File could not be accessed.");
        }

        try (Scanner fileScanner = new Scanner(credsFile)) {
            while (fileScanner.hasNextLine()) {
                String entry = fileScanner.nextLine();
                Matcher m = r.matcher(entry);

                if (m.find()) {
                    String username = m.group("username");
                    String password = m.group("password");

                    credentials.put(username, password);
                } else {
                    throw new InvalidParameterException("Regex could not match a username or password group.");
                }
            }
        } catch (FileNotFoundException e) {
            // File wasn't found.
            e.printStackTrace();
        }
    }

    public boolean authenticateUser(String username, String password) {
        if (credentials.containsKey(username) && credentials.get(username).equals(password)) {
            authenticatedUser = username;
            return true;
        }

        return false;
    }

    public void logoutUser() {
        authenticatedUser = "";
    }

    public String getAuthenticatedUser() {
        return authenticatedUser;
    }

    public String[] getAllUsernames() {
        return credentials.keySet()
                .toArray(new String[credentials.keySet().size()]);
    }
}
