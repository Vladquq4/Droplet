package services;

import com.google.gson.Gson;
import main.GameSystem;
import models.Achievement;
import models.CardInfo;
import models.Game;  // Assuming you have a Game class
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

            // Create user_games table (if it doesn't exist)
            stmt.execute("""
                    CREATE TABLE IF NOT EXISTS user_games (
                        user_id INTEGER NOT NULL,
                        game_id INTEGER NOT NULL,
                        FOREIGN KEY(user_id) REFERENCES users(id),
                        FOREIGN KEY(game_id) REFERENCES games(id)
                    )
                    """);

            // Create Games table (if it doesn't exist)
            stmt.execute("""
                    CREATE TABLE IF NOT EXISTS games (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        name TEXT NOT NULL,
                        description TEXT NOT NULL
                    )
                    """);

        } catch (SQLException e) {
            throw new RuntimeException("Error initializing database: " + e.getMessage(), e);
        }
    }

    // Save a list of users to the database
    public static void saveUsers(List<User> users) {
        try (Connection conn = DriverManager.getConnection(DATABASE_URL)) {
            String insertOrReplaceQuery = """
            INSERT OR REPLACE INTO users (id, username, password, wallet)
            VALUES (
                (SELECT id FROM users WHERE username = ?),
                ?, ?, ?
            )
            """;

            try (PreparedStatement stmt = conn.prepareStatement(insertOrReplaceQuery)) {
                for (User user : users) {
                    stmt.setString(1, user.getUsername()); // For the subquery
                    stmt.setString(2, user.getUsername());
                    stmt.setString(3, user.getPassword());
                    stmt.setDouble(4, user.getWallet());
                    stmt.executeUpdate();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }



    public static List<User> loadUsers() {
        List<User> users = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(DATABASE_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM users")) {

            while (rs.next()) {
                // Create the User object
                String username = rs.getString("username");
                String password = rs.getString("password");
                double wallet = rs.getDouble("wallet");

                User user = new User(username, password, wallet);

                // Set the ID from the database
                user.setId(rs.getInt("id"));

                System.out.println("Loaded user: ID = " + user.getId() + ", Username = " + username);

                // Add the user to the list
                users.add(user);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return users;
    }


    public static boolean deleteUser(String username) {
        try (Connection conn = DriverManager.getConnection(DATABASE_URL)) {
            // Delete related records first
            String deleteUserGamesSQL = "DELETE FROM user_games WHERE user_id = (SELECT id FROM users WHERE username = ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(deleteUserGamesSQL)) {
                pstmt.setString(1, username);
                pstmt.executeUpdate();
            }

            String deleteCardInfoSQL = "DELETE FROM card_info WHERE username = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(deleteCardInfoSQL)) {
                pstmt.setString(1, username);
                pstmt.executeUpdate();
            }

            String deleteUserSQL = "DELETE FROM users WHERE username = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(deleteUserSQL)) {
                pstmt.setString(1, username);
                int rowsAffected = pstmt.executeUpdate();
                return rowsAffected > 0; // Return true if a user was deleted
            }
        } catch (SQLException e) {
            System.out.println("Error deleting user: " + e.getMessage());
            return false;
        }
    }
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
    public static void saveCardInfos(String username, CardInfo card) {
        try (Connection conn = DriverManager.getConnection(DATABASE_URL)) {
            String query = "INSERT INTO card_info (username, cardholder_name, card_number, expiry_date, cvv) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, username);
            stmt.setString(2, card.getCardholderName());
            stmt.setString(3, card.getCardNumber());
            stmt.setString(4, card.getExpiryDate());
            stmt.setString(5, card.getCvv());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
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
    public static List<CardInfo> loadAllCards(String username) {
        List<CardInfo> cards = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(DATABASE_URL)) {
            String query = "SELECT * FROM card_info WHERE username = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                cards.add(new CardInfo(
                        rs.getString("cardholder_name"),
                        rs.getString("card_number"),
                        rs.getString("expiry_date"),
                        rs.getString("cvv")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return cards;
    }
    public static List<Game> getUserGames(String username) {
        List<Game> userGames = new ArrayList<>();

        try (Connection conn = DriverManager.getConnection(DATABASE_URL)) {
            // Step 1: Fetch the user ID based on username
            String userQuery = "SELECT id FROM users WHERE username = ?";
            try (PreparedStatement userStmt = conn.prepareStatement(userQuery)) {
                userStmt.setString(1, username);
                ResultSet userResult = userStmt.executeQuery();

                if (userResult.next()) {
                    int userId = userResult.getInt("id");

                    // Step 2: Fetch game details using JOIN for efficiency
                    String fetchGamesQuery = """
                    SELECT g.id, g.name, g.price, g.genre
                    FROM games g
                    INNER JOIN user_games ug ON g.id = ug.game_id
                    WHERE ug.user_id = ?
                """;
                    try (PreparedStatement fetchGamesStmt = conn.prepareStatement(fetchGamesQuery)) {
                        fetchGamesStmt.setInt(1, userId);
                        ResultSet gameResult = fetchGamesStmt.executeQuery();

                        // Step 3: Process the result set and create Game objects
                        while (gameResult.next()) {
                            int gameId = gameResult.getInt("id");
                            String gameName = gameResult.getString("name");
                            float gamePrice = gameResult.getFloat("price");
                            String gameGenre = gameResult.getString("genre");

                            // If you need achievements, fetch them separately or include them in the query
                            List<Achievement> achievements = new ArrayList<>(); // Placeholder for achievements
                            Game game = new Game(gameId, gameName, gameGenre, gamePrice, achievements);
                            userGames.add(game);
                        }
                    }
                } else {
                    System.out.println("No user found with username: " + username);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching games for user: " + username);
            e.printStackTrace();
        }

        if (userGames.isEmpty()) {
            System.out.println("No games found for user: " + username);
        }

        return userGames;
    }


}
