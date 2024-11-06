package models;

import interfaces.I;

import java.util.List;

public class Game implements I, Comparable<Game> {
    private String name;
    private float price;
    private String genre;
    private List<Achievement> achievements;

    public Game(String name, float price, String genre, List<Achievement> achievements) {
        this.name = name;
        this.price = price;
        this.genre = genre;
        this.achievements = achievements;
    }

    public String getName() { return name; }
    public float getPrice() { return price; }
    public String getGenre() { return genre; }
    public List<Achievement> getAchievements() { return achievements; }

    public void addAchievement(Achievement achievement) {
        achievements.add(achievement);
        System.out.println("Achievement added: " + achievement);
    }

    public void sortAchievements() {
        achievements.sort(null); // Uses compareTo from Achievement
        System.out.println("Achievements sorted alphabetically:");
        achievements.forEach(System.out::println);
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
        return Float.compare(this.price, other.price); // Compare by price
    }
    @Override
    public void purchase() {
        System.out.println("Game " + name + " has been purchased for $" + price);
    }

    @Override
    public String toString() {
        return "Game: " + name + ", Price: $" + price + ", Genre: " + genre;
    }
}
