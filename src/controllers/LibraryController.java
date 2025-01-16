package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import models.Game;
import models.User;
import services.UserDataManager;
import models.TwitchAuthService;
import javafx.scene.Scene;
import javafx.fxml.FXMLLoader;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class LibraryController {

    @FXML
    private ListView<String> gamesListView;
    @FXML
    private ImageView gameCoverImage;
    @FXML
    private Label gameNameLabel;
    @FXML
    private Label gameGenreLabel;
    @FXML
    private Label gamePriceLabel;
    @FXML
    private Button downloadButton;
    @FXML
    private Button backButton;

    private User loggedInUser;
    private Game selectedGame; // Track the currently selected game

    public void initialize(User user) {
        this.loggedInUser = user;

        if (loggedInUser != null) {
            List<Game> userGames = UserDataManager.getUserGames(loggedInUser.getUsername());

            for (Game game : userGames) {
                gamesListView.getItems().add(game.getName());
            }

            gamesListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue != null) {
                    selectedGame = findGameByName(newValue, userGames);
                    if (selectedGame != null) {
                        displayGameDetails(selectedGame);
                    }
                }
            });
        }
    }
    private Game findGameByName(String gameName, List<Game> userGames) {
        for (Game game : userGames) {
            if (game.getName().equals(gameName)) {
                return game;
            }
        }
        return null;
    }
    private void displayGameDetails(Game game) {
        String coverUrl = TwitchAuthService.getGameCover(game.getName());
        if (coverUrl != null) {
            gameCoverImage.setImage(new Image(coverUrl, 150, 200, true, true));
        } else {
            gameCoverImage.setImage(new Image("/images/default_cover.png", 150, 200, true, true));
        }

        gameNameLabel.setText(game.getName());
        gameGenreLabel.setText("Genre: " + game.getGenre());
        gamePriceLabel.setText("Price: $" + game.getPrice());

        downloadButton.setVisible(true);
    }
    @FXML
    private void handleBackButton() {
        try {
            FXMLLoader storeLoader = new FXMLLoader(getClass().getResource("/fxml/store.fxml"));
            AnchorPane storeRoot = storeLoader.load();

            StoreController storeController = storeLoader.getController();
            storeController.setCurrentUser(loggedInUser);

            Stage stage = (Stage) backButton.getScene().getWindow();
            stage.setScene(new Scene(storeRoot));
            stage.setTitle("Droplet Store");

        } catch (IOException e) {
            System.err.println("Error loading store screen: " + e.getMessage());
            e.printStackTrace();
        }
    }
    @FXML
    private void downloadLink() {
        if (selectedGame != null) {
            try {
                // Generate the URL for the selected game
                String gameName = URLEncoder.encode(selectedGame.getName(), StandardCharsets.UTF_8);
                gameName = gameName.replace("+", "%20"); // Replace '+' with '%20'

                String downloadUrl = "https://thepiratebay0.org/search/" + gameName + "/";

                // Open the URL in the default browser
                if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                    Desktop.getDesktop().browse(new URI(downloadUrl));
                } else {
                    System.err.println("Desktop browsing not supported on this platform.");
                }
            } catch (Exception e) {
                System.err.println("Error opening download link: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.out.println("No game selected for download.");
        }
    }
}
