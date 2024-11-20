package services;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import main.GameSystem;
import models.User;
import models.CardInfo;

import java.io.*;
import java.lang.reflect.Type;
import java.util.*;

import static main.GameSystem.loginUser;

public class UserDataManager {
    private static final String USER_FILE = "users.json";
    private static final String CARD_DATA_FILE = "cardData.json";
    private static final Gson gson = new Gson();

    public static void saveUsers(List<User> users) {
        try (FileWriter writer = new FileWriter(USER_FILE)) {
            gson.toJson(users, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static List<User> loadUsers() {
        File file = new File(USER_FILE);
        if (!file.exists()) {
            System.out.println("User file not found: " + USER_FILE);
            throw new RuntimeException(new FileNotFoundException("File not found: " + USER_FILE));
        }

        try (FileReader reader = new FileReader(USER_FILE)) {
            Type userListType = new TypeToken<ArrayList<User>>(){}.getType();
            return gson.fromJson(reader, userListType);
        } catch (IOException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
    public static boolean deleteUser(List<User> users, String username) {
        User userToDelete = null;
        for (User user : users) {
            if (user.getUsername().equals(username)) {
                userToDelete = user;
                break;
            }
        }

        if (userToDelete != null) {
            users.remove(userToDelete);
            saveUsers(users);
            return true;
        }
        return false; // User not found
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
        // Try to load the session from the file
        try (FileReader reader2 = new FileReader("session.json")) {
            Gson gson = new Gson();
            // Parse the session data into a map
            Map<String, String> sessionData = gson.fromJson(reader2, Map.class);

            // If the session contains both username and password, try logging in automatically
            if (sessionData != null && sessionData.containsKey("username") && sessionData.containsKey("password")) {
                String username = sessionData.get("username");
                String password = sessionData.get("password");

                System.out.println("Previous session found, attempting to log in automatically...");
                return GameSystem.loginUserSession(users, username, password);
            }
        } catch (IOException e) {
            System.out.println("No previous session found or error loading session.");
        }

        // If no valid session or error occurred, fall back to manual login
        System.out.println("No active session. Please log in.");
        return null;
    }
    public static void deleteSession() {
        File sessionFile = new File("session.json");
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
    public static CardInfo loadCardInfo(String username) {
        Map<String, CardInfo> cardInfoMap = loadAllCardInfo();
        return cardInfoMap != null ? cardInfoMap.get(username) : null;
    }
    public static Map<String, CardInfo> loadAllCardInfo() {
        try (FileReader reader = new FileReader("cardData.json")) {
            Type cardInfoType = new TypeToken<Map<String, CardInfo>>() {}.getType();
            return gson.fromJson(reader, cardInfoType);
        } catch (IOException e) {
            System.out.println("Error loading card info: " + e.getMessage());
            return new HashMap<>();
        }
    }
    public static void saveCardInfo(String username, String cardholderName, String cardNumber, String expiryDate, String cvv) {
        try (FileReader reader = new FileReader("cardData.json")) {
            Type cardInfoType = new TypeToken<Map<String, CardInfo>>() {}.getType();
            Map<String, CardInfo> cardInfoMap = gson.fromJson(reader, cardInfoType);
            if (cardInfoMap == null) {
                cardInfoMap = new HashMap<>();
            }
            cardInfoMap.put(username, new CardInfo(cardholderName, cardNumber, expiryDate, cvv));
            try (FileWriter writer = new FileWriter("cardData.json")) {
                gson.toJson(cardInfoMap, writer);
            }
        } catch (IOException e) {
            System.out.println("Error saving card info: " + e.getMessage());
        }
    }
}