package models;

import interfaces.J;

public class Achievement implements J, Comparable<Achievement> {
    private String name;

    public Achievement(String name) {
        this.name = name;
    }
    @Override
    public int compareTo(Achievement other) {
        return this.name.compareTo(other.name); // Compare alphabetically by name
    }

    @Override
    public void share() {
        System.out.println("Sharing achievement: " + name);
    }

    @Override
    public String toString() {
        return "Achievement: " + name;
    }

}