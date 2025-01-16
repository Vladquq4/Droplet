package models;

import interfaces.I;
import org.junit.Test;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static services.UserDataManager.getUserGames;

public class Game implements I, Comparable<Game> {
    private int id;
    private String name;
    private float price;
    private String genre;
    private List<Achievement> achievements;
    public void testGameConstructor() throws Exception {
        int id = 1;
        String name = "GameName";
        String genre = "Adventure";
        float price = 29.99f;
        List<Achievement> achievements = new ArrayList<>();

        // Use reflection to create a Game instance
        Constructor<Game> constructor = Game.class.getConstructor(int.class, String.class, String.class, float.class, List.class);
        Game game = constructor.newInstance(id, name, genre, price, achievements);

        // Assert that the fields are correctly initialized
        assertEquals(1, game.getId());
        assertEquals("GameName", game.getName());
        assertEquals("Adventure", game.getGenre());
        assertEquals(29.99f, game.getPrice(), 0.0f);
        assertNotNull(game.getAchievements());
    }
    public Game(int id, String name, String genre, float price, List<Achievement> achievements) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.genre = genre;
        this.achievements = achievements;
    }
    public String getName() {
        return name;
    }
    public float getPrice() {
        return price;
    }
    public String getGenre() {
        return genre;
    }
    public List<Achievement> getAchievements() {
        return achievements;
    }
    public void addAchievement(Achievement achievement) {
        achievements.add(achievement);
        System.out.println("Achievement added: " + achievement);
    }
    public void sortAchievements() {
        achievements.sort(null);
        System.out.println("Achievements sorted alphabetically:");
        achievements.forEach(System.out::println);
    }
    public static void printUserGameLibrary(User username) {
        // Fetch the user's games from the database
        List<Game> userGames = getUserGames(username.getUsername());

        // Check if the game library is empty
        if (userGames.isEmpty()) {
            System.out.println("Your game library is empty.");
        } else {
            System.out.println("Your Game Library:");
            // Iterate over the games and print details of each game
            for (Game game : userGames) {
                System.out.println("Game: " + game.getName() + ", Genre: " + game.getGenre() +
                        ", Price: $" + game.getPrice());
            }
        }
    }
    public void shareAchievement(String achievementName) {
        for (Achievement achievement : achievements) {
            if (achievement.toString().contains(achievementName)) {
                achievement.share();
                return;
            }
        }
        System.out.println("Achievement not found: " + achievementName);
    }
    @Override
    public int compareTo(Game other) {
        return Float.compare(this.price, other.price);
    }
    @Override
    public void purchase() {
        System.out.println("Game " + name + " has been purchased for $" + price);
    }
    @Override
    public String toString() {
        return "Game: " + name + ", Price: $" + price + ", Genre: " + genre;
    }
    public int getId() {return id;}
}
