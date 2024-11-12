package main;

import exceptions.DuplicateUserException;
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

        User loggedInUser;

        if (args.length == 1 && args[0].equalsIgnoreCase("continue")) {
            loggedInUser = UserDataManager.loadSession(users);
            if (loggedInUser != null) {
                System.out.println("Continuing session for " + loggedInUser.getUsername());
                showMenu(scanner, store, loggedInUser, users);
            } else {
                System.out.println("No saved session found. Please log in or register.");
                startNewSession(scanner, store, users);
            }
        } else if (args.length == 3 && args[0].equalsIgnoreCase("login")) {
            String username = args[1];
            String password = args[2];
            loggedInUser = findUserByName(users, username);

            if (loggedInUser != null && loggedInUser.getPassword().equals(password)) {
                System.out.println("Welcome, " + loggedInUser.getUsername() + "!");
                UserDataManager.saveSession(loggedInUser);
                showMenu(scanner, store, loggedInUser, users);
            } else {
                System.out.println("Login failed: Invalid username or password.");
            }
        } else {
            System.out.println("Enter 'new' to start a new session or 'continue' to resume the last session:");
            String sessionChoice = scanner.nextLine().toLowerCase();

            if ("continue".equals(sessionChoice)) {
                loggedInUser = UserDataManager.loadSession(users);
                if (loggedInUser != null) {
                    System.out.println("Continuing session for " + loggedInUser.getUsername());
                    showMenu(scanner, store, loggedInUser, users);
                } else {
                    System.out.println("No saved session found. Starting new session.");
                    startNewSession(scanner, store, users);
                }
            } else {
                startNewSession(scanner, store, users);
            }
        }

        scanner.close();
    }
    private static void showMenu(Scanner scanner, Store store, User loggedInUser, List<User> users) {
        boolean running = true;
        while (running) {
            delayMenu();
            System.out.println("\n--- Main Menu ---");
            System.out.println("1. Browse Store");
            System.out.println("2. Social");
            System.out.println("3. Account");
            System.out.println("4. Logout");
            System.out.print("Choose an option: ");
            if (users.isEmpty() || !users.contains(loggedInUser)) {
                running = false;
                break;
            }

            String choice = scanner.nextLine();
            switch (choice) {
                case "1":
                    showBrowseStoreMenu(scanner, store, loggedInUser);
                    break;
                case "2":
                    showSocialMenu(scanner, users, loggedInUser);
                    break;
                case "3":
                    showAccountMenu(scanner, loggedInUser, users);
                    break;
                case "4":
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
    private static void showBrowseStoreMenu(Scanner scanner, Store store, User loggedInUser) {
        boolean running = true;
        while (running) {
            System.out.println("\n--- Browse Store ---");
            System.out.println("1. Browse Games");
            System.out.println("2. Purchase Game");
            System.out.println("3. Sort Games by Price");
            System.out.println("4. Sort Games by Name");
            System.out.println("5. Group Games by Genre");
            System.out.println("6. Back to Main Menu");
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
                    System.out.println("Sorting games by price...");
                    store.sortGamesByPrice();
                    break;
                case "4":
                    store.getAvailableGames().sort(Comparator.comparing(Game::getName));
                    store.browseStore();
                    break;
                case "5":
                    System.out.println("Grouping games by genre...");
                    store.groupGamesByGenre();
                    break;
                case "6":
                    running = false;
                    break;
                default:
                    System.out.println("Invalid option. Please try again.");
                    break;
            }
        }
    }
    private static void showSocialMenu(Scanner scanner, List<User> users, User loggedInUser) {
        boolean running = true;
        while (running) {
            System.out.println("\n--- Social ---");
            System.out.println("1. Sort Users by Username");
            System.out.println("2. Add Friend");
            System.out.println("3. Inbox");
            System.out.println("4. Back to Main Menu");
            System.out.print("Choose an option: ");

            String choice = scanner.nextLine();
            switch (choice) {
                case "1":
                    System.out.println("Sorting users by username...");
                    users.sort(User::compareTo);
                    users.forEach(System.out::println);
                    break;
                case "2":
                    System.out.println("Add Friend option - no logic for now.");
                    break;
                case "3":
                    System.out.println("Inbox option - no logic for now.");
                    break;
                case "4":
                    running = false;
                    break;
                default:
                    System.out.println("Invalid option. Please try again.");
                    break;
            }
        }
    }
    private static void showAccountMenu(Scanner scanner, User loggedInUser, List<User> users) {
        boolean running = true;
        while (running) {
            System.out.println("\n--- Account ---");
            System.out.println("1. View Account");
            System.out.println("2. Delete Account");
            System.out.println("3. Add Funds");
            System.out.println("4. Back to Main Menu");
            System.out.print("Choose an option: ");

            String choice = scanner.nextLine();
            switch (choice) {
                case "1":
                    System.out.println("Account Username: " + loggedInUser.getUsername());
                    System.out.println("Wallet Balance: $" + loggedInUser.getWallet());
                    break;
                case "2":
                    loggedInUser.deleteAccount(scanner, users);

                    if (users.isEmpty() || !users.contains(loggedInUser)) {
                        running = false;
                    }
                    break;
                case "3":
                    loggedInUser.addFunds(scanner);
                    break;
                case "4":
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
    private static User registerUser(List<User> users, Scanner scanner) throws DuplicateUserException {
        System.out.print("Enter username: ");
        String username = scanner.nextLine();

        for (User user : users) {
            if (user.getUsername().equals(username)) {
                throw new DuplicateUserException("Username already exists. Please choose another.");
            }
        }

        System.out.print("Enter password: ");
        String password = scanner.nextLine();

        User newUser = new User(username, password);
        users.add(newUser);
        UserDataManager.saveUsers(users);
        System.out.println("User registered: " + username);
        return newUser;
    }
    public static User loginUser(List<User> users, Scanner scanner) {
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
    public static User loginUserSession(List<User> users, String username, String password) {
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
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            e.printStackTrace();
        }
    }
    private static User findUserByName(List<User> users, String username) {
        return users.stream().filter(u -> u.getUsername().equals(username)).findFirst().orElse(null);
    }
    private static Game findGameByName(List<Game> games, String name) {
        return games.stream().filter(g -> g.getName().equals(name)).findFirst().orElse(null);
    }
    private static User startNewSession(Scanner scanner, Store store, List<User> users) {
        System.out.println("Enter 'register' to create a new account or 'login' to sign in:");
        String command = scanner.nextLine().toLowerCase();
        User newUser = null;
        switch (command) {
            case "register":
                boolean validUsername = false;
                while (!validUsername) {
                    try {
                        newUser = registerUser(users, scanner);
                        validUsername = true;
                    } catch (DuplicateUserException e) {
                        System.out.println(e.getMessage());
                        System.out.println("Please choose a different username.");
                    }
                }
                break;
            case "login":
                newUser = loginUser(users, scanner);
                break;
            default:
                System.out.println("Invalid command.");
        }

        if (newUser != null) {
            UserDataManager.saveSession(newUser);
            showMenu(scanner, store, newUser, users);
        }
        return newUser;
    }

}