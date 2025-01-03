package services;

import com.google.gson.Gson;
import main.GameSystem;
import models.CardInfo;
import models.User;

import java.io.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserDataManager {
    private static final String DATABASE_URL = "jdbc:sqlite:store.db";
    private static final String SESSION_FILE = "session.json";

    // Initialize database and create tables
    static {
        try (Connection conn = DriverManager.getConnection(DATABASE_URL)) {
            Statement stmt = conn.createStatement();

            // Create Users table
            stmt.execute("""
                    CREATE TABLE IF NOT EXISTS users (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        username TEXT UNIQUE NOT NULL,
                        password TEXT NOT NULL,
                        wallet REAL DEFAULT 0.0
                    )
                    """);

            // Create Card Info table
            stmt.execute("""
                    CREATE TABLE IF NOT EXISTS card_info (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        username TEXT NOT NULL,
                        cardholder_name TEXT NOT NULL,
                        card_number TEXT NOT NULL,
                        expiry_date TEXT NOT NULL,
                        cvv TEXT NOT NULL,
                        FOREIGN KEY(username) REFERENCES users(username)
                    )
                    """);
        } catch (SQLException e) {
            throw new RuntimeException("Error initializing database: " + e.getMessage(), e);
        }
    }

    // Save a list of users to the database
    public static void saveUsers(List<User> users) {
        try (Connection conn = DriverManager.getConnection(DATABASE_URL)) {
            String insertUserQuery = """
                    INSERT OR REPLACE INTO users (username, password, wallet)
                    VALUES (?, ?, ?)
                    """;
            PreparedStatement stmt = conn.prepareStatement(insertUserQuery);

            for (User user : users) {
                stmt.setString(1, user.getUsername());
                stmt.setString(2, user.getPassword());
                stmt.setDouble(3, user.getWallet());
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Load all users from the database
    public static List<User> loadUsers() {
        List<User> users = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(DATABASE_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM users")) {

            while (rs.next()) {
                User user = new User(
                        rs.getString("username"),
                        rs.getString("password"),
                        rs.getDouble("wallet")
                );
                users.add(user);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return users;
    }
    public static boolean deleteUser(String username) {
        String deleteUserSQL = "DELETE FROM users WHERE username = ?";
        try (Connection conn = DriverManager.getConnection(DATABASE_URL);
             PreparedStatement pstmt = conn.prepareStatement(deleteUserSQL)) {

            pstmt.setString(1, username);
            int rowsAffected = pstmt.executeUpdate();

            return rowsAffected > 0; // Return true if a user was deleted
        } catch (SQLException e) {
            System.out.println("Error deleting user: " + e.getMessage());
            return false;
        }
    }

    // Save the session to a JSON file
    public static void saveSession(User user) {
        try (FileWriter writer2 = new FileWriter("session.json")) {
            Map<String, String> sessionData = new HashMap<>();
            sessionData.put("username", user.getUsername());
            sessionData.put("password", user.getPassword());
            Gson gson = new Gson();
            gson.toJson(sessionData, writer2);
        } catch (IOException e) {
            System.out.println("Error saving session: " + e.getMessage());
        }
    }

    // Load the session from a JSON file
    public static User loadSession(List<User> users) {
        try (FileReader reader = new FileReader(SESSION_FILE)) {
            Gson gson = new Gson();
            Map<String, String> sessionData = gson.fromJson(reader, Map.class);

            if (sessionData != null && sessionData.containsKey("username") && sessionData.containsKey("password")) {
                String username = sessionData.get("username");
                String password = sessionData.get("password");

                System.out.println("Previous session found, attempting to log in automatically...");
                return GameSystem.loginUserSession(users, username, password);
            }
        } catch (IOException e) {
            System.out.println("No previous session found or error loading session.");
        }

        System.out.println("No active session. Please log in.");
        return null;
    }

    // Delete the session JSON file
    public static void deleteSession() {
        File sessionFile = new File(SESSION_FILE);
        if (sessionFile.exists()) {
            if (sessionFile.delete()) {
                System.out.println("Session file deleted successfully.");
            } else {
                System.out.println("Failed to delete session file.");
            }
        } else {
            System.out.println("No session file found to delete.");
        }
    }

    // Save card information to the database
    public static void saveCardInfo(String username, String cardholderName, String cardNumber, String expiryDate, String cvv) {
        try (Connection conn = DriverManager.getConnection(DATABASE_URL)) {
            String insertCardQuery = """
                    INSERT INTO card_info (username, cardholder_name, card_number, expiry_date, cvv)
                    VALUES (?, ?, ?, ?, ?)
                    """;
            PreparedStatement stmt = conn.prepareStatement(insertCardQuery);
            stmt.setString(1, username);
            stmt.setString(2, cardholderName);
            stmt.setString(3, cardNumber);
            stmt.setString(4, expiryDate);
            stmt.setString(5, cvv);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Load card information for a user
    public static CardInfo loadCardInfo(String username) {
        try (Connection conn = DriverManager.getConnection(DATABASE_URL)) {
            String loadCardQuery = "SELECT * FROM card_info WHERE username = ?";
            PreparedStatement stmt = conn.prepareStatement(loadCardQuery);
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return new CardInfo(
                        rs.getString("cardholder_name"),
                        rs.getString("card_number"),
                        rs.getString("expiry_date"),
                        rs.getString("cvv")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null; // No card info found
    }
}
