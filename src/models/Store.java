package models;

import exceptions.InvalidGameException;

import java.lang.reflect.Constructor;
import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;


public class Store {
    private static final String DATABASE_URL ="jdbc:sqlite:store.db" ;
    private List<Game> availableGames;
    private final Connection connection;
    private final ExecutorService executorService;

    public void testStoreConstructor() throws Exception {
        Connection connection = null;

        Constructor<Store> constructor = Store.class.getConstructor(Connection.class);
        Store store = constructor.newInstance(connection);

        assertNotNull(store);
        assertEquals(connection, store.getConnectionn());
    }
    public Store(Connection connection) {
        this.connection = connection;
        this.executorService = Executors.newCachedThreadPool();
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
    public void sortGamesByPriceAsync() {
        executorService.submit(() -> {
            availableGames.sort(Comparator.comparingDouble(Game::getPrice));
            System.out.println("Games sorted by price:");
            availableGames.forEach(System.out::println);
        });
    }
    public void groupGamesByGenreAsync() {
        executorService.submit(() -> {
            Map<String, List<Game>> gamesByGenre = availableGames.stream()
                    .collect(Collectors.groupingBy(Game::getGenre));

            System.out.println("Games grouped by genre:");
            gamesByGenre.forEach((genre, games) -> {
                System.out.println("Genre: " + genre);
                games.forEach(System.out::println);
            });
        });
    }
    public boolean purchaseGame(User user, Game game) throws InvalidGameException {
        if (game == null) {
            throw new InvalidGameException("The selected game does not exist.");
        }
        if (game.getPrice() < 0) {
            throw new InvalidGameException("The game '" + game.getName() + "' has an invalid price: $" + game.getPrice());
        }

        if (user.getWallet() >= game.getPrice()) { // Check if user has enough funds
            String addGameToLibraryQuery = "INSERT INTO user_games (user_id, game_id) VALUES (?, ?)";
            String deductFundsQuery = "UPDATE Users SET wallet = wallet - ? WHERE id = ?";

            try (Connection connection = DriverManager.getConnection(DATABASE_URL);
                 PreparedStatement addStmt = connection.prepareStatement(addGameToLibraryQuery);
                 PreparedStatement deductStmt = connection.prepareStatement(deductFundsQuery)) {

                connection.setAutoCommit(false); // Start transaction

                // Add game to library
                addStmt.setInt(1, user.getId());
                addStmt.setInt(2, game.getId());
                addStmt.executeUpdate();

                // Deduct funds
                BigDecimal gamePrice = BigDecimal.valueOf(game.getPrice()); // Convert float to BigDecimal
                deductStmt.setBigDecimal(1, gamePrice);
                deductStmt.setInt(2, user.getId());
                deductStmt.executeUpdate();

                connection.commit(); // Commit transaction
                user.addGameToLibrary(game); // Update in-memory user object
                user.deductFunds(game.getPrice()); // Update wallet balance

                System.out.println("Purchase successful. " + game.getName() + " added to your library.");
                return true;

            } catch (SQLException e) {
                try {
                    if (connection != null) connection.rollback(); // Rollback on error
                } catch (SQLException rollbackEx) {
                    rollbackEx.printStackTrace();
                }
                throw new RuntimeException("Error during game purchase for user ID " + user.getId(), e);
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
    public List<Game> searchGames(String query) {
        List<Game> searchResults = new ArrayList<>();
        String searchQuery = "SELECT * FROM Games WHERE LOWER(name) LIKE ?";

        try (PreparedStatement stmt = connection.prepareStatement(searchQuery)) {
            stmt.setString(1, "%" + query.toLowerCase() + "%");

            try (ResultSet rs = stmt.executeQuery()) {
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

                    Game game = new Game(gameId, gameName, genre, price, achievements);
                    searchResults.add(game);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return searchResults;
    }
    public List<Game> getAvailableGames() {
        return availableGames;
    }

    public Connection getConnectionn() {
        return connection;
    }
}