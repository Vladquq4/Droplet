package models;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import exceptions.InvalidGameException;

import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Store {
    private List<Game> availableGames;

    public Store(String jsonFilePath) {
        this.availableGames = loadGamesFromJson(jsonFilePath);
    }

    private List<Game> loadGamesFromJson(String jsonFilePath) {
        Gson gson = new Gson();
        try (FileReader reader = new FileReader(jsonFilePath)) {
            Type gameListType = new TypeToken<List<Game>>() {}.getType();
            List<Game> games = gson.fromJson(reader, gameListType);

            return games;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
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
            user.addGameToLibrary(game);
            user.deductFunds(game.getPrice());
            System.out.println("Purchase successful. " + game.getName() + " added to your library.");
        } else {
            System.out.println("Insufficient funds to purchase " + game.getName() + ".");
        }
        return true;
    }

    public void browseStore() {
        if (availableGames != null) {
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
