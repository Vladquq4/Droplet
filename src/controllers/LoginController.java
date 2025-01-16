package controllers;

import exceptions.DuplicateUserException;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import models.User;
import services.UserDataManager;

import java.io.IOException;
import java.sql.Connection;
import java.util.List;
import java.util.function.Consumer;

public class LoginController{

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Button loginButton;

    @FXML
    private Button registerButton;

    @FXML
    private Label errorLabel;

    private List<User> users;
    private Connection connection;
    private Consumer<User> onLoginSuccess; // Consumer to handle successful login

    public void initialize(Connection connection, List<User> users, Consumer<User> onLoginSuccess) {
        this.connection = connection;
        this.users = users;
        this.onLoginSuccess = onLoginSuccess;
    }
    @FXML
    private void handleLogin() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        User loggedInUser = loginUser(users, username, password);

        if (loggedInUser != null) {
            // Save session only if login is successful
            UserDataManager.saveSession(loggedInUser);

            errorLabel.setVisible(false);
            System.out.println("Welcome, " + loggedInUser.getUsername() + "!");

            // Trigger the login success callback with the logged-in user
            onLoginSuccess.accept(loggedInUser);

            // After successful login, change the scene to the store
            changeSceneToStore(loggedInUser);
        } else {
            // Show an error message if login fails
            errorLabel.setText("Invalid username or password!");
            errorLabel.setVisible(true);
        }
    }
    private void changeSceneToStore(User loggedInUser) {
        // Ensure the login button is valid and get the stage
        String username;
        String password;

        Stage stage = (Stage) loginButton.getScene().getWindow(); // Assuming loginButton is valid
        if (stage != null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/store.fxml"));
                Parent storeRoot = loader.load();

                // Optionally, get the controller for the store
                StoreController storeController = loader.getController();

                // Set up any data in the storeController if needed
                storeController.setCurrentUser(loggedInUser);

                // Switch the scene
                stage.setScene(new Scene(storeRoot));
                stage.setTitle("Droplet Store");
            } catch (IOException e) {
                e.printStackTrace(); // Handle any exception related to loading the FXML
            }
        }
    }
    @FXML
    private void handleRegister() {
        try {
            User newUser = registerUser(users);
            errorLabel.setVisible(false);
            System.out.println("User registered: " + newUser.getUsername());
        } catch (DuplicateUserException e) {
            errorLabel.setText(e.getMessage());
            errorLabel.setVisible(true);
        }
    }
    private User loginUser(List<User> users, String username, String password) {
        for (User user : users) {
            if (user.getUsername().equals(username) && user.getPassword().equals(password)) {
                return user;
            }
        }
        return null;
    }
    private User registerUser(List<User> users) throws DuplicateUserException {
        String username = usernameField.getText();
        for (User user : users) {
            if (user.getUsername().equals(username)) {
                throw new DuplicateUserException("Username already exists. Please choose another.");
            }
        }

        String password = passwordField.getText();
        User newUser = new User(username, password, 0.00);
        users.add(newUser);
        UserDataManager.saveUsers(users);
        return newUser;
    }
}
