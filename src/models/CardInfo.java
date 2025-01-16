package models;

import org.junit.Test;

import java.lang.reflect.Constructor;

import static org.junit.Assert.assertEquals;

public class CardInfo {

    public void testCardInfoConstructor() throws Exception {
        String cardholderName = "John Doe";
        String cardNumber = "1234567890123456";
        String expiryDate = "12/24";
        String cvv = "123";

        // Use reflection to create a CardInfo instance
        Constructor<CardInfo> constructor = CardInfo.class.getConstructor(String.class, String.class, String.class, String.class);
        CardInfo cardInfo = constructor.newInstance(cardholderName, cardNumber, expiryDate, cvv);

        // Assert that the fields are correctly initialized
        assertEquals("John Doe", cardInfo.getCardholderName());
        assertEquals("1234567890123456", cardInfo.getCardNumber());
        assertEquals("12/24", cardInfo.getExpiryDate());
        assertEquals("123", cardInfo.getCvv());
    }
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
