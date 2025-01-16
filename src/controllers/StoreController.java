package controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import models.Game;
import models.Store;
import models.TwitchAuthService;
import models.User;
import services.UserDataManager;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.List;

public class StoreController  {

    @FXML private TilePane gameTilePane; // Linked to the TilePane in FXML
    @FXML private VBox gameListVBox; // Linked to the VBox (if you decide to use this for another purpose)
    @FXML private Button loginButton;
    @FXML private Button accountButton;
    @FXML private Button libraryButton;
    @FXML private TextField searchField;
    @FXML private AnchorPane anchorPane; // Linked to AnchorPane in FXML

    private Store store;
    private List<User> users;
    private User currentUser;
    private Connection connection;

    public void setCurrentUser(User user) {
        this.currentUser = user;
    }
    public void initialize(Connection connection, List<User> users, User currentUser) {
        this.connection = connection;
        this.store = new Store(connection);
        this.users = users;
        this.currentUser = currentUser;
        if (this.connection != null) {
            try {
                Statement stmt = this.connection.createStatement();
                // Proceed with query execution...
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            System.err.println("Connection is null.");
        }
        displayGames(); // Load and display games in the store
    }
    public void onLogoutClicked() {
        System.out.println("Log out button clicked!");
        UserDataManager.saveUsers(Collections.singletonList(currentUser));
        UserDataManager.deleteSession();
        currentUser = null;

        // Navigate back to the login screen
        try {
            FXMLLoader loginLoader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
            VBox loginRoot = loginLoader.load();

            // Get the LoginController and set up dependencies
            LoginController loginController = loginLoader.getController();
            loginController.initialize(connection, users, (user) -> {
                transitionToStoreScreen();
            });

            // Set the login scene on the current stage
            Stage stage = (Stage) loginButton.getScene().getWindow();
            stage.setScene(new Scene(loginRoot));
            stage.setTitle("Login to Droplet");
        } catch (IOException e) {
            System.err.println("Error loading login screen: " + e.getMessage());
            e.printStackTrace();
        }
    }
    private void transitionToStoreScreen() {
        try {
            FXMLLoader storeLoader = new FXMLLoader(getClass().getResource("/fxml/store.fxml"));
            AnchorPane storeRoot = storeLoader.load();

            StoreController storeController = storeLoader.getController();
            storeController.initialize(connection, users, currentUser);

            // Switch to store view
            Stage stage = (Stage) loginButton.getScene().getWindow();
            stage.setScene(new Scene(storeRoot));
            stage.setTitle("Droplet Store");
        } catch (IOException e) {
            System.err.println("Error loading store screen: " + e.getMessage());
            e.printStackTrace();
        }
    }
    public void onAccountClicked() {
        System.out.println("Account settings button clicked!");
        try {
            FXMLLoader accountSettingsLoader = new FXMLLoader(getClass().getResource("/fxml/accountsetting.fxml"));
            AnchorPane accountSettingsRoot = accountSettingsLoader.load();

            AccountSettingsController accountSettingsController = accountSettingsLoader.getController();
            if (currentUser != null) {
                accountSettingsController.setCurrentUser(currentUser);
            } else {
                System.err.println("Warning: Current user is null.");
            }

            Stage stage = (Stage) accountButton.getScene().getWindow();
            Scene scene = new Scene(accountSettingsRoot);
            stage.setScene(scene);
            stage.setTitle("Account Settings");
        } catch (IOException e) {
            System.err.println("Error loading account settings screen: " + e.getMessage());
            e.printStackTrace();
        }
    }
    public void onLibraryClicked() {
        System.out.println("Library button clicked!");
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/library.fxml"));
            Parent libraryRoot = loader.load();
            LibraryController libraryController = loader.getController();
            libraryController.initialize(currentUser);

            Stage stage = (Stage) libraryButton.getScene().getWindow();
            stage.setScene(new Scene(libraryRoot));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void onSearchClicked() {
        String query = searchField.getText().trim();
        if (!query.isEmpty()) {
            searchGames(query);
        } else {
            displayGames();
        }
    }
    public void displayGames() {
        gameTilePane.getChildren().clear();
        List<Game> games = store.getAvailableGames();
        System.out.println("I printed games");
        for (Game game : games) {
            addGameCard(game);
        }
    }
    private void searchGames(String query) {
        gameTilePane.getChildren().clear();
        List<Game> games = store.searchGames(query);
        for (Game game : games) {
            addGameCard(game);
        }
    }
    private void addGameCard(Game game) {
        Label titleLabel = new Label(game.getName());
        Label priceLabel = new Label("$" + String.format("%.2f", game.getPrice()));
        priceLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #ff9900;");

        ImageView coverImageView = new ImageView();
        String coverUrl = TwitchAuthService.getGameCover(game.getName());
        if (coverUrl != null) {
            coverImageView.setImage(new Image(coverUrl, 150, 200, true, true));
        } else {
            coverImageView.setImage(new Image("/images/default_cover.png", 150, 200, true, true));
        }

        Button purchaseButton = new Button("Purchase");
        purchaseButton.setOnAction(event -> onPurchaseClicked(game));

        VBox gameCard = new VBox(coverImageView, titleLabel, priceLabel, purchaseButton);
        gameCard.getStyleClass().add("game-card");

        gameTilePane.getChildren().add(gameCard);
    }
    private void onPurchaseClicked(Game game) {
        System.out.println("Purchasing game: " + game.getName());
        try {
            boolean purchaseSuccessful = store.purchaseGame(currentUser, game);
            if (purchaseSuccessful) {
                System.out.println("Purchase successful! " + game.getName() + " added to your library.");
            } else {
                System.out.println("Purchase failed.");
            }
        } catch ( RuntimeException e) {
            System.out.println("Error purchasing game: " + e.getMessage());
        }
    }
}
