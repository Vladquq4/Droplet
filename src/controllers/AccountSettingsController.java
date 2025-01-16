package controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import models.CardInfo;
import models.User;
import services.UserDataManager;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class AccountSettingsController  {

    @FXML
    public Button deleteAccountButton;
    @FXML
    private Label accountInfoLabel;

    @FXML
    private VBox cardInfoVBox;

    @FXML
    private ComboBox<CardInfo> cardComboBox;

    @FXML
    private Button storeButton;


    @FXML
    private Button addFundsButton;

    @FXML
    private Button addNewCardButton;

    @FXML
    private TextField amountTextField;

    private User currentUser;


    @FXML
    private void initialize() {
        if (currentUser != null) {
            // Display the username and wallet balance
            accountInfoLabel.setText("Account Username: " + currentUser.getUsername() + "\nWallet Balance: $" + currentUser.getWallet());

            // Load and display cards associated with the current user
            loadCardInfo();
        }
    }
    private void loadCardInfo() {
        CardInfo cardInfo = UserDataManager.loadCardInfo(currentUser.getUsername());
        cardInfoVBox.getChildren().clear(); // Clear previous data

        if (cardInfo == null) {
            // If no card exists, show the option to add a new card
            cardComboBox.setVisible(false); // Hide ComboBox if no cards exist
            addNewCardButton.setVisible(true); // Show "Add New Card" button
        } else {
            // Display the single card in ComboBox
            cardComboBox.setVisible(true); // Show ComboBox with cards
            addNewCardButton.setVisible(false); // Hide "Add New Card" button
            cardComboBox.getItems().setAll(cardInfo); // Add single card
        }
    }
    @FXML
    private void onDeleteAccountClicked() {
        Alert confirmationAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmationAlert.setTitle("Delete Account");
        confirmationAlert.setHeaderText("Are you sure you want to delete your account?");
        confirmationAlert.setContentText("This action cannot be undone.");

        if (confirmationAlert.showAndWait().get() == ButtonType.OK) {
            // Call deleteAccount on the current user
            try {
                // Delete the user from the database (assuming deleteUser will handle it)
                StoreController storeController = new StoreController();
                currentUser.deleteUser(storeController);

                // Print message for debugging
                System.out.println("Account deleted successfully.");

                // Forcefully exit the application
                Platform.exit();  // This will close the entire app.

            } catch (Exception e) {
                System.out.println("Error deleting account: " + e.getMessage());
            }
        }
    }
    @FXML
    private void onAddFundsClicked() {
        // Create a dialog to handle card selection or addition
        Dialog<CardInfo> cardDialog = new Dialog<>();
        cardDialog.setTitle("Add Funds");
        cardDialog.setHeaderText("Select a card or add a new one.");

        // Fetch existing cards
        List<CardInfo> userCards = UserDataManager.loadAllCards(currentUser.getUsername());

        // Create the UI for the dialog
        VBox dialogContent = new VBox(10);
        dialogContent.setStyle("-fx-padding: 10;");

        ComboBox<CardInfo> cardSelectionBox = new ComboBox<>();
        cardSelectionBox.setPromptText("Select an existing card");

        TextField cardholderNameField = new TextField();
        cardholderNameField.setPromptText("Cardholder Name");

        TextField cardNumberField = new TextField();
        cardNumberField.setPromptText("Card Number");

        TextField expiryDateField = new TextField();
        expiryDateField.setPromptText("Expiry Date (MM/YY)");

        TextField cvvField = new TextField();
        cvvField.setPromptText("CVV");

        ButtonType submitButtonType = new ButtonType("Submit", ButtonBar.ButtonData.OK_DONE);
        cardDialog.getDialogPane().getButtonTypes().addAll(submitButtonType, ButtonType.CANCEL);

        // If cards exist, add the ComboBox to the dialog
        if (!userCards.isEmpty()) {
            cardSelectionBox.getItems().addAll(userCards);
            dialogContent.getChildren().addAll(new Label("Select an existing card:"), cardSelectionBox);
        }

        // Add fields for adding a new card
        dialogContent.getChildren().addAll(
                new Label("Or enter new card details:"),
                cardholderNameField, cardNumberField, expiryDateField, cvvField
        );

        cardDialog.getDialogPane().setContent(dialogContent);

        // Handle the dialog result
        cardDialog.setResultConverter(dialogButton -> {
            if (dialogButton == submitButtonType) {
                if (cardSelectionBox.getValue() != null) {
                    // Return the selected card
                    return cardSelectionBox.getValue();
                } else {
                    // Validate and return a new card
                    if (cardholderNameField.getText().isEmpty() ||
                            cardNumberField.getText().isEmpty() ||
                            expiryDateField.getText().isEmpty() ||
                            cvvField.getText().isEmpty()) {
                        showAlert("Error", "Please fill in all card details.");
                        return null;
                    }
                    return new CardInfo(
                            cardholderNameField.getText(),
                            cardNumberField.getText(),
                            expiryDateField.getText(),
                            cvvField.getText()
                    );
                }
            }
            return null;
        });

        // Show the dialog
        Optional<CardInfo> result = cardDialog.showAndWait();

        result.ifPresent(selectedCard -> {
            // Add funds logic
            try {
                float amount = Float.parseFloat(amountTextField.getText());

                if (amount <= 0) {
                    throw new IllegalArgumentException("Amount must be greater than zero.");
                }

                // Save new card to the database if it's a new card
                if (!userCards.contains(selectedCard)) {
                    UserDataManager.saveCardInfos(currentUser.getUsername(), selectedCard);
                }

                // Add funds to the user's account
                currentUser.addFund(amount);
                System.out.println("Funds added successfully! New balance: $" + currentUser.getWallet());
                showAlert("Success", "Funds added successfully!");

                // Save updated user data to the database
                UserDataManager.saveUsers(Collections.singletonList(currentUser));  // Save only the current user

            } catch (NumberFormatException e) {
                showAlert("Error", "Invalid amount entered. Please enter a valid number.");
            } catch (IllegalArgumentException e) {
                showAlert("Error", e.getMessage());
            }
        });
    }
    @FXML
    private void onStoreButtonClicked() {
        try {

            // Load the Store scene
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/store.fxml"));  // Update with the correct path to the Store FXML file
            Parent storeRoot = loader.load();

            StoreController storeController = loader.getController();
            storeController.setCurrentUser(currentUser);
            // Get the current stage (window) and set the new scene
            Stage stage = (Stage) storeButton.getScene().getWindow();
            Scene storeScene = new Scene(storeRoot);
            stage.setScene(storeScene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to switch to Store scene.");
        }
    }
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    @FXML
    private void onAddNewCardClicked() {
        // Show a prompt for the user to enter card details (or open a new screen for this)
        System.out.println("User wants to add a new card.");
        // You could open another screen here for the card details input form
    }
    public void setCurrentUser(User currentUser) {
        this.currentUser = currentUser;
        initialize();
    }
}
