package models;

import interfaces.I;

public class Transaction implements I {
    private String gameName;
    private float amount;

    public Transaction(String gameName, float amount) {
        this.gameName = gameName;
        this.amount = amount;
    }

    @Override
    public void purchase() {
        System.out.println("Processing purchase for game: " + gameName + " - Amount: $" + amount);
    }

    @Override
    public String toString() {
        return "Transaction: " + gameName + " - $" + amount;
    }

}