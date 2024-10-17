import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

abstract class UserOperations {
    public abstract void addPassword(PasswordEntry entry);
    public abstract PasswordEntry searchPassword(String appName);
    public abstract boolean deletePassword(String appName);
    public abstract void saveUserData();
}

class User extends UserOperations {
    private String name;
    private String password;
    private HashMap<String, PasswordEntry> passwords;

    public User(String name, String password) {
        this.name = name;
        this.password = password;
        this.passwords = new HashMap<>();
    }

    @Override
    public void addPassword(PasswordEntry entry) {
        if (validateEntry(entry)) {
            passwords.put(entry.getAppName(), entry);
            saveUserData();
        } else {
            throw new IllegalArgumentException("Invalid input. Make sure all fields are non-null and password is strong.");
        }
    }

    @Override
    public PasswordEntry searchPassword(String appName) {
        return passwords.get(appName);
    }

    @Override
    public boolean deletePassword(String appName) {
        boolean removed = passwords.remove(appName) != null;
        if (removed) {
            saveUserData();
        }
        return removed;
    }

    public void saveUserData() {
        PasswordManagerApp.saveUserData(this);
    }

    public String getName() {
        return name;
    }

    public String getPassword() {
        return password;
    }

    public HashMap<String, PasswordEntry> getPasswords() {
        return passwords;
    }

    private boolean validateEntry(PasswordEntry entry) {
        return entry.getAppName() != null && entry.getUsername() != null && entry.getPassword() != null && PasswordManagerApp.isPasswordStrong(entry.getPassword());
    }
}

class PasswordEntry {
    private String appName;
    private String username;
    private String password;

    public PasswordEntry(String appName, String username, String password) {
        this.appName = appName;
        this.username = username;
        this.password = password;
    }

    public String getAppName() {
        return appName;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    @Override
    public String toString() {
        return appName + "," + username + "," + password;
    }
}

public class PasswordManagerApp {
    private static final String FILE_PATH = "user_data.db";
    private static Map<String, User> userDatabase = new HashMap<>();
    private static User currentUser;

    public static void main(String[] args) {
        loadUserData();
        createAndShowGUI();
    }

    private static void loadUserData() {
        try {
            if (Files.exists(Paths.get(FILE_PATH))) {
                BufferedReader reader = new BufferedReader(new FileReader(FILE_PATH));
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] parts = line.split(";");
                    if (parts.length > 1) {
                        User user = new User(parts[0], parts[1]);
                        for (int i = 2; i < parts.length; i++) {
                            String[] entryParts = parts[i].split(",");
                            PasswordEntry entry = new PasswordEntry(entryParts[0], entryParts[1], entryParts[2]);
                            user.addPassword(entry);
                        }
                        userDatabase.put(user.getName(), user);
                    }
                }
                reader.close();
            }
        } catch (IOException e) {
            System.out.println(e);
        }
    }

    public static void saveUserData(User user) {
        try (Writer writer = new FileWriter(FILE_PATH)) {
            for (User u : userDatabase.values()) {
                StringBuilder userLine = new StringBuilder(u.getName() + ";" + u.getPassword());
                for (PasswordEntry entry : u.getPasswords().values()) {
                    userLine.append(";").append(entry.toString());
                }
                writer.write(userLine.toString() + "\n");
            }
        } catch (IOException e) {
            System.out.println(e);
        }
    }

    private static void createAndShowGUI() {
        JFrame frame = new JFrame("Password Manager");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 300);
        frame.setLayout(new BorderLayout());

        JPanel panel = new JPanel();
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        panel.setLayout(new GridLayout(0, 2, 10, 10));

        JTextField usernameField = new JTextField();
        JPasswordField passwordField = new JPasswordField();
        JButton loginButton = new JButton("Login");
        JButton registerButton = new JButton("Register");

        panel.add(new JLabel("Username:"));
        panel.add(usernameField);
        panel.add(new JLabel("Password:"));
        panel.add(passwordField);
        panel.add(loginButton);
        panel.add(registerButton);

        frame.add(panel, BorderLayout.CENTER);
        frame.setVisible(true);

        loginButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String username = usernameField.getText();
                String password = new String(passwordField.getPassword());

                if (isInputValid(username, password) && userDatabase.containsKey(username) && userDatabase.get(username).getPassword().equals(password)) {
                    currentUser = userDatabase.get(username);
                    frame.dispose();
                    showUserInterface();
                } else {
                    JOptionPane.showMessageDialog(frame, "Invalid username or password.");
                }
            }
        });

        registerButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String username = usernameField.getText();
                String password = new String(passwordField.getPassword());

                if (isInputValid(username, password) && !userDatabase.containsKey(username)) {
                    if (isPasswordStrong(password)) {
                        User newUser = new User(username, password);
                        userDatabase.put(username, newUser);
                        saveUserData(newUser);
                        JOptionPane.showMessageDialog(frame, "User registered successfully.");
                    } else {
                        JOptionPane.showMessageDialog(frame, "Password must be 8 characters long, contain upper, lower, number, and special character.");
                    }
                } else {
                    JOptionPane.showMessageDialog(frame, "Invalid input or user already exists.");
                }
            }
        });
    }

    private static void showUserInterface() {
        JFrame userFrame = new JFrame("User Interface");
        userFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        userFrame.setSize(400, 400);
        userFrame.setLayout(new BorderLayout());

        JPanel panel = new JPanel();
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        panel.setLayout(new GridLayout(0, 2, 10, 10));

        JTextField appNameField = new JTextField();
        JTextField usernameField = new JTextField();
        JPasswordField passwordField = new JPasswordField();
        JButton addButton = new JButton("Add Password");
        JButton searchButton = new JButton("Search Password");
        JButton deleteButton = new JButton("Delete Password");

        panel.add(new JLabel("App Name:"));
        panel.add(appNameField);
        panel.add(new JLabel("Username:"));
        panel.add(usernameField);
        panel.add(new JLabel("Password:"));
        panel.add(passwordField);
        panel.add(addButton);
        panel.add(searchButton);
        panel.add(deleteButton);

        userFrame.add(panel, BorderLayout.CENTER);
        userFrame.setVisible(true);

        addButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String appName = appNameField.getText();
                String username = usernameField.getText();
                String password = new String(passwordField.getPassword());

                if (isInputValid(username, password) && isPasswordStrong(password)) {
                    PasswordEntry entry = new PasswordEntry(appName, username, password);
                    try {
                        currentUser.addPassword(entry);
                        JOptionPane.showMessageDialog(userFrame, "Password added.");
                    } catch (IllegalArgumentException ex) {
                        JOptionPane.showMessageDialog(userFrame, ex.getMessage());
                    }
                } else {
                    JOptionPane.showMessageDialog(userFrame, "Invalid input or weak password.");
                }
            }
        });

        searchButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String appName = appNameField.getText();
                PasswordEntry foundEntry = currentUser.searchPassword(appName);

                if (foundEntry != null) {
                    JOptionPane.showMessageDialog(userFrame, "App: " + foundEntry.getAppName() +
                            "\nUsername: " + foundEntry.getUsername() +
                            "\nPassword: " + foundEntry.getPassword());
                } else {
                    JOptionPane.showMessageDialog(userFrame, "No data found for the given application.");
                }
            }
        });

        deleteButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String appName = appNameField.getText();
                boolean removed = currentUser.deletePassword(appName);

                if (removed) {
                    JOptionPane.showMessageDialog(userFrame, "Password deleted.");
                } else {
                    JOptionPane.showMessageDialog(userFrame, "No application found with the given name.");
                }
            }
        });
    }

    public static boolean isPasswordStrong(String password) {
        if (password == null || password.length() < 8) return false;

        boolean hasUpperCase = false, hasLowerCase = false, hasDigit = false, hasSpecialChar = false;
        String specialChars = "@$!%*?&#";

        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c)) hasUpperCase = true;
            else if (Character.isLowerCase(c)) hasLowerCase = true;
            else if (Character.isDigit(c)) hasDigit = true;
            else if (specialChars.indexOf(c) >= 0) hasSpecialChar = true;

            if (hasUpperCase && hasLowerCase && hasDigit && hasSpecialChar) return true;
        }
        return false;
    }

    public static boolean isInputValid(String username, String password) {
        return username != null && !username.trim().isEmpty() && password != null && !password.trim().isEmpty();
    }
}
