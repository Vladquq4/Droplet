package models;

public class CardInfo {
    private String cardholderName;
    private String cardNumber;
    private String expiryDate;
    private String cvv;

    public CardInfo(String cardholderName, String cardNumber, String expiryDate, String cvv) {
        this.cardholderName = cardholderName;
        this.cardNumber = cardNumber;
        this.expiryDate = expiryDate;
        this.cvv = cvv;
    }

    public String getCardholderName() { return cardholderName; }
    public String getCardNumber() { return cardNumber; }
    public String getExpiryDate() { return expiryDate; }
    public String getCvv() { return cvv; }
}
