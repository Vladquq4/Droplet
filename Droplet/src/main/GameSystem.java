package main;


import models.Game;
import models.Store;
import models.User;
import services.UserDataManager;

import java.util.Comparator;
import java.util.Scanner;
import java.util.List;

public class GameSystem {
    public static void main(String[] args) {

        Store store = new Store("C:\\Users\\vlada\\IdeaProjects\\Droplet\\games.json");
        List<User> users = UserDataManager.loadUsers();
        Scanner scanner = new Scanner(System.in);

        System.out.println("Welcome to Droplet!");

        if (args.length == 3 && args[0].equalsIgnoreCase("login")) {
            String username = args[1];
            String password = args[2];
            User loggedInUser = findUserByName(users, username);

            if (loggedInUser != null && loggedInUser.getPassword().equals(password)) {
                System.out.println("Welcome, " + loggedInUser.getUsername() + "!");
                showMenu(scanner, store, loggedInUser, users);
            } else {
                System.out.println("Login failed: Invalid username or password.");
            }
        } else {

            System.out.println("Enter 'register' to create a new account or 'login' to sign in:");
            String command = scanner.nextLine();

            switch (command.toLowerCase()) {
                case "register":
                    registerUser(users, scanner);
                    break;
                case "login":
                    User loggedInUser = loginUser(users, scanner);
                    if (loggedInUser != null) {
                        System.out.println("Welcome, " + loggedInUser.getUsername() + "!");
                        showMenu(scanner, store, loggedInUser, users);
                    }
                    break;
                default:
                    System.out.println("Invalid command.");
                    break;
            }
        }

        scanner.close();
    }

    private static void showMenu(Scanner scanner, Store store, User loggedInUser, List<User> users) {
        boolean running = true;
        while (running) {
            delayMenu();
            System.out.println("\n--- Main Menu ---");
            System.out.println("1. Browse Games");
            System.out.println("2. Purchase Game");
            System.out.println("3. View Account");
            System.out.println("4. Sort Games by Price");
            System.out.println("5. Sort Games by Name");
            System.out.println("6. Sort Users by Username");
            System.out.println("7. Group Games by Genre");
            System.out.println("8. Logout");
            System.out.print("Choose an option: ");

            String choice = scanner.nextLine();
            switch (choice) {
                case "1":
                    store.browseStore();
                    break;
                case "2":
                    purchaseGame(scanner, store, loggedInUser);
                    break;
                case "3":
                    System.out.println("Account Username: " + loggedInUser.getUsername());
                    break;
                case "4":
                    System.out.println("Sorting games by price...");
                    store.sortGamesByPrice();
                    break;
                case "5":
                    store.getAvailableGames().sort(Comparator.comparing(Game::getName));
                    store.browseStore();
                    break;
                case "6":
                    System.out.println("Sorting users by username...");
                    users.sort(User::compareTo);
                    users.forEach(System.out::println);
                    break;
                case "7":
                    System.out.println("Grouping games by genre...");
                    store.groupGamesByGenre();
                    break;
                case "8":
                    System.out.println("Logging out...");
                    UserDataManager.saveUsers(users);
                    running = false;
                    break;
                default:
                    System.out.println("Invalid option. Please try again.");
                    break;
            }
        }
    }

    private static void purchaseGame(Scanner scanner, Store store, User user) {
        System.out.print("Enter the name of the game to purchase: ");
        String gameName = scanner.nextLine();
        Game game = findGameByName(store.getAvailableGames(), gameName);

        if (game != null && store.purchaseGame(user, game)) {
            System.out.println("Game purchased: " + gameName);
        } else {
            System.out.println("Purchase failed. Game may not exist or insufficient funds.");
        }
    }

    private static void registerUser(List<User> users, Scanner scanner) {
        System.out.print("Enter username: ");
        String username = scanner.nextLine();

        for (User user : users) {
            if (user.getUsername().equals(username)) {
                System.out.println("Username already exists. Please choose another.");
                return;
            }
        }

        System.out.print("Enter password: ");
        String password = scanner.nextLine();

        User newUser = new User(username, password);
        users.add(newUser);
        UserDataManager.saveUsers(users);
        System.out.println("User registered: " + username);
    }
    private static User loginUser(List<User> users, Scanner scanner) {
        System.out.print("Enter username: ");
        String username = scanner.nextLine();

        System.out.print("Enter password: ");
        String password = scanner.nextLine();

        for (User user : users) {
            if (user.getUsername().equals(username) && user.getPassword().equals(password)) {
                System.out.println("Login successful: " + username);
                return user;
            }
        }

        System.out.println("Login failed: Invalid username or password.");
        return null;
    }
    private static void delayMenu() {
        try {
            Thread.sleep(1000); // Pauses execution for 1000 milliseconds (1 second)
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // Restore interrupted status
            e.printStackTrace();
        }
    }
    private static User findUserByName(List<User> users, String username) {
        return users.stream().filter(u -> u.getUsername().equals(username)).findFirst().orElse(null);
    }

    private static Game findGameByName(List<Game> games, String name) {
        return games.stream().filter(g -> g.getName().equals(name)).findFirst().orElse(null);
    }
}