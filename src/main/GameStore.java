package main;

import controllers.LoginController;
import controllers.StoreController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import models.User;
import services.UserDataManager;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;

import static javafx.application.Application.launch;

public class GameStore extends Application {

    public static void main(String[] args) {
        launch(args);
    }


    public void start(Stage primaryStage) {
        Connection connection = null;
        try {
            // Establish a connection to the database
            connection = DriverManager.getConnection("jdbc:sqlite:store.db");
        } catch (SQLException e) {
            // Show a dialog with the error message if database connection fails
            Alert alert = new Alert(Alert.AlertType.ERROR, "Failed to connect to the database. Please try again later.");
            alert.showAndWait();
            e.printStackTrace();
            return;
        }

        try {
            // Load users and current session user
            List<User> users = UserDataManager.loadUsers();
            User currentUser = UserDataManager.loadSession(users);

            if (currentUser == null) {
                // Load the login screen if no user is logged in
                FXMLLoader loginLoader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
                VBox loginRoot = loginLoader.load();

                // Get the LoginController and set up dependencies
                LoginController loginController = loginLoader.getController();
                Connection finalConnection = connection;
                loginController.initialize(connection, users, (user) -> {
                    // Transition to the store screen upon successful login
                    loadStoreScreen(primaryStage, finalConnection, users, user);
                });

                primaryStage.setScene(new Scene(loginRoot));
                primaryStage.setTitle("Login to Droplet");
                primaryStage.show();
            } else {
                // Load the store screen directly if the user is logged in
                loadStoreScreen(primaryStage, connection, users, currentUser);
            }

        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR, "An error occurred while loading the application. Please try again.");
            alert.showAndWait();
        }
    }

    private void loadStoreScreen(Stage primaryStage, Connection connection, List<User> users, User currentUser) {
        try {
            FXMLLoader storeLoader = new FXMLLoader(getClass().getResource("/fxml/store.fxml"));
            AnchorPane storeRoot = storeLoader.load();

            StoreController storeController = storeLoader.getController();
            storeController.initialize(connection, users, currentUser);

            primaryStage.setScene(new Scene(storeRoot));
            primaryStage.setTitle("Droplet Game Store");
            primaryStage.show();
        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR, "An error occurred while loading the store screen.");
            alert.showAndWait();
        }
    }
}

