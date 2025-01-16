package models;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import services.UserDataManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;
import controllers.StoreController;
import static main.GameSystem.validateTextInput;

public class User implements Comparable<User>{
    private int id;
    private String username;
    private String password;
    private double wallet;
    private List<Game> library;
    private List<User> friends;

    // Constructor
    public User(String username, String password, double wallet) {
        this.username = username;
        this.password = password;
        this.wallet = wallet;
        this.library = new ArrayList<>();
        this.friends = new ArrayList<>();
    }

    public void setId(int id) {
        this.id = id;
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
    public void addFund(float amount) {
            wallet += amount;
            System.out.println("Funds added successfully. New balance: $" + wallet);
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
            boolean success = UserDataManager.deleteUser(this.username);
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
    public void deleteUser(StoreController storeController) {

        Alert confirmationAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmationAlert.setTitle("Delete Account");
        confirmationAlert.setHeaderText("Are you sure you want to delete your account?");
        confirmationAlert.setContentText("This action cannot be undone.");

        // Wait for user response
        Optional<ButtonType> result = confirmationAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            // If confirmed, proceed to delete the account
            boolean success = UserDataManager.deleteUser(this.username);
            if (success) {
                // Inform the user that their account was successfully deleted
                Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                successAlert.setTitle("Account Deleted");
                successAlert.setHeaderText("Your account has been deleted.");
                successAlert.showAndWait();

                // Log out the user and clear the session
                UserDataManager.deleteSession();
            } else {
                // In case of failure
                Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                errorAlert.setTitle("Deletion Failed");
                errorAlert.setHeaderText("Account deletion failed.");
                errorAlert.setContentText("Please try again later.");
                errorAlert.showAndWait();
            }
        } else {
            // If canceled, show cancellation message
            Alert cancelAlert = new Alert(Alert.AlertType.INFORMATION);
            cancelAlert.setTitle("Deletion Canceled");
            cancelAlert.setHeaderText("Account deletion was canceled.");
            cancelAlert.showAndWait();
        }
    }

    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public double getWallet() { return wallet; }
    public List<Game> getLibrary() { return library; }
    public List<User> getFriends() { return friends; }

    public int getId() { return id;}
}