package models;

import services.UserDataManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import static main.GameSystem.validateTextInput;

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

    public void addFunds(Scanner scanner) {
        System.out.println("Choose a payment option:");
        System.out.println("1. Use Existing Card");
        System.out.println("2. Add New Card");
        String choice = validateTextInput(scanner, "Your choice: ", 1, 1);

        if (choice.equals("1")) {
            CardInfo cardInfo = UserDataManager.loadCardInfo(username);
            if (cardInfo != null) {
                String lastFourDigits = cardInfo.getCardNumber().substring(cardInfo.getCardNumber().length() - 4);
                System.out.println("Using card: " + cardInfo.getCardholderName() + " **** " + lastFourDigits);

                float amount = validatePositiveFloatInput(scanner, "Enter amount to add: ");
                wallet += amount;
                System.out.println("Funds added successfully. New balance: $" + wallet);
            } else {
                System.out.println("No existing card found. Please add a new card.");
                choice = "2";
            }
        }

        if (choice.equals("2")) {
            String cardholderName = validateTextInput(scanner, "Enter Cardholder Name: ", 3, 50);
            String cardNumber = validateCardNumber(scanner);
            String expiryDate = validateExpiryDate(scanner);
            String cvv = validateTextInput(scanner, "Enter CVV: ", 3, 3);

            float amount = validatePositiveFloatInput(scanner, "Enter amount to add: ");
            wallet += amount;
            System.out.println("Funds added successfully. New balance: $" + wallet);

            UserDataManager.saveCardInfo(username, cardholderName, cardNumber, expiryDate, cvv);
        } else {
            System.out.println("Invalid choice.");
        }
    }
    public static float validatePositiveFloatInput(Scanner scanner, String prompt) {
        while (true) {
            try {
                System.out.print(prompt);
                float value = Float.parseFloat(scanner.nextLine().trim());
                if (value <= 0) {
                    System.out.println("Value must be positive. Please try again.");
                } else {
                    return value;
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid number. Please enter a valid positive number.");
            }
        }
    }

    public static String validateCardNumber(Scanner scanner) {
        while (true) {
            System.out.print("Enter Card Number: ");
            String cardNumber = scanner.nextLine().trim();
            if (cardNumber.matches("\\d{16}")) {
                return cardNumber;
            } else {
                System.out.println("Invalid card number. Please enter a 16-digit number.");
            }
        }
    }

    public static String validateExpiryDate(Scanner scanner) {
        while (true) {
            System.out.print("Enter Expiry Date (MM/YY): ");
            String expiryDate = scanner.nextLine().trim();
            if (expiryDate.matches("(0[1-9]|1[0-2])/\\d{2}")) {
                return expiryDate;
            } else {
                System.out.println("Invalid expiry date. Please use the format MM/YY.");
            }
        }
    }
    private void validateAmount(float amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero.");
        }
    }
    public boolean deductFunds(float amount) {
        if (wallet >= amount) {
            wallet -= amount;
            System.out.println("Funds deducted successfully. New balance: $" + wallet);
            return true;
        } else {
            System.out.println("Insufficient funds for this transaction.");
            return false;
        }
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