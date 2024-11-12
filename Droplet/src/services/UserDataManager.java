package services;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import models.User;

import java.io.File;
import java.io.FileWriter;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class UserDataManager {
    private static final String USER_FILE = "users.json";
    private static final Gson gson = new Gson();

    public static void saveUsers(List<User> users) {
        try (FileWriter writer = new FileWriter(USER_FILE)) {
            gson.toJson(users, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static List<User> loadUsers() {
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
            Gson gson = new Gson();
            gson.toJson(user, writer2);
        } catch (IOException e) {
            System.out.println("Error saving session: " + e.getMessage());
        }
    }

    public static User loadSession() {
        try (FileReader reader2 = new FileReader("session.json")) {
            Gson gson = new Gson();
            return gson.fromJson(reader2, User.class);
        } catch (IOException e) {
            System.out.println("No previous session found or error loading session.");
            return null;
        }
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
}