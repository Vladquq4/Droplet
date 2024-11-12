package models;

import services.UserDataManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class User implements Comparable<User>{
    private String username;
    private String password;
    private float wallet;
    private List<Game> library;
    private List<User> friends;

    // Constructor
    public User(String username, String password) {
        this.username = username;
        this.password = password;
        this.wallet = 0.00f;
        this.library = new ArrayList<>();
        this.friends = new ArrayList<>();
    }

    @Override
    public int compareTo(User other) {
        return this.username.compareTo(other.username);
    }

    @Override
    public String toString() {
        return "User: " + username + ", Wallet: $" + wallet + ", Games in Library: " + library.size();
    }


    public void addGameToLibrary(Game game) {
        library.add(game);
    }

    public void addFunds(float amount) {
        wallet += amount;
    }

    public boolean hasGameInLibrary(Game game) {
        return library.contains(game);
    }
    public void deleteAccount(Scanner scanner, List<User> users) {
        System.out.print("Type 'DELETE' to confirm: ");
        String confirmDelete = scanner.nextLine().toLowerCase();
        if (confirmDelete.equals("delete")) {
            boolean success = UserDataManager.deleteUser(users, this.username);
            if (success) {
                System.out.println("Your account has been deleted.");
                UserDataManager.deleteSession();
            } else {
                System.out.println("Account deletion failed. Please try again.");
            }
        } else {
            System.out.println("Account deletion cancelled.");
        }
    }


    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public float getWallet() { return wallet; }
    public List<Game> getLibrary() { return library; }
    public List<User> getFriends() { return friends; }
}