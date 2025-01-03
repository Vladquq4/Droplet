package models;

import exceptions.InvalidGameException;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Store {
    private List<Game> availableGames;
    private final Connection connection;

    public Store(Connection connection) {
        this.connection = connection;
        this.availableGames = loadGamesFromDatabase();
    }

    private List<Game> loadGamesFromDatabase() {
        List<Game> games = new ArrayList<>();
        String query = "SELECT * FROM Games";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                int gameId = rs.getInt("id");
                String gameName = rs.getString("name");
                float price = rs.getFloat("price");
                String genre = rs.getString("genre");

                // Fetch achievements related to this game
                List<Achievement> achievements = new ArrayList<>();
                String achievementQuery = "SELECT name FROM achievements WHERE game_id = ?";
                try (PreparedStatement achievementStmt = connection.prepareStatement(achievementQuery)) {
                    achievementStmt.setInt(1, gameId);
                    try (ResultSet achievementRs = achievementStmt.executeQuery()) {
                        while (achievementRs.next()) {
                            String achievementName = achievementRs.getString("name");
                            Achievement achievement = new Achievement(achievementName);
                            achievements.add(achievement);
                        }
                    }
                }

                // Create the Game object with achievements
                Game game = new Game(gameId, gameName, genre, price, achievements);
                games.add(game);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return games;
    }


    public void sortGamesByPrice() {
        availableGames.sort(null);
        System.out.println("Games sorted by price:");
        availableGames.forEach(System.out::println);
    }

    public void groupGamesByGenre() {
        Map<String, List<Game>> gamesByGenre = availableGames.stream()
                .collect(Collectors.groupingBy(Game::getGenre));

        System.out.println("Games grouped by genre:");
        gamesByGenre.forEach((genre, games) -> {
            System.out.println("Genre: " + genre);
            games.forEach(System.out::println);
        });
    }

    public boolean purchaseGame(User user, Game game) throws InvalidGameException {
        if (game == null) {
            throw new InvalidGameException("The selected game does not exist.");
        }
        if (game.getPrice() < 0) {
            throw new InvalidGameException("The game '" + game.getName() + "' has an invalid price: $" + game.getPrice());
        }

        if (user.getWallet() >= game.getPrice()) {
            // Add game to user's library in the database
            String addGameToLibraryQuery = "INSERT INTO UserGames (user_id, game_id) VALUES (?, ?)";
            String deductFundsQuery = "UPDATE Users SET wallet = wallet - ? WHERE id = ?";

            try (PreparedStatement addStmt = connection.prepareStatement(addGameToLibraryQuery);
                 PreparedStatement deductStmt = connection.prepareStatement(deductFundsQuery)) {

                connection.setAutoCommit(false); // Start transaction

                // Add game to library
                addStmt.setInt(1, user.getId());
                addStmt.setInt(2, game.getId());
                addStmt.executeUpdate();

                // Deduct funds
                deductStmt.setDouble(1, game.getPrice());
                deductStmt.setInt(2, user.getId());
                deductStmt.executeUpdate();

                connection.commit(); // Commit transaction
                user.addGameToLibrary(game); // Update in-memory user object
                user.deductFunds(game.getPrice());

                System.out.println("Purchase successful. " + game.getName() + " added to your library.");
                return true;

            } catch (SQLException e) {
                try {
                    connection.rollback(); // Rollback on error
                } catch (SQLException rollbackEx) {
                    rollbackEx.printStackTrace();
                }
                e.printStackTrace();
            } finally {
                try {
                    connection.setAutoCommit(true); // Restore auto-commit
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        } else {
            System.out.println("Insufficient funds to purchase " + game.getName() + ".");
        }
        return false;
    }

    public void browseStore() {
        if (availableGames != null && !availableGames.isEmpty()) {
            for (Game game : availableGames) {
                System.out.println(game.toString());
            }
        } else {
            System.out.println("No games available in the store.");
        }
    }

    public List<Game> getAvailableGames() {
        return availableGames;
    }
}
