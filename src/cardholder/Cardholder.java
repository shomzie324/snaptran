package cardholder;

import card.Card;
import java.io.Serializable;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Observable;
import log.LogManager;
import serialize.SerializeManager;

/**
 * Represent a cardholder with name and email. A cardholder stores its cards by cardId. A cardholder
 * can change its name but cannot change its email. A cardholder can also get a card given cardId,
 * add or remove a card from its card bag.
 */
public class Cardholder extends Observable implements Serializable {

  private final String email;
  /*a card bag to store cards of this cardholder, use cardId as key*/
  private final HashMap<String, Card> myCards;
  private final String password;
  private String name;
  private Calendar registerDate;

  /**
   * Create a new cardholder account.
   *
   * @param email email address of the cardholder(It can not be changed).
   * @param name the name of this cardholder.
   */
  private Cardholder(String name, String email, String password, Calendar registerDate) {
    this.name = name;
    this.email = email;
    this.registerDate = Calendar.getInstance();
    /* create a new card bag to store cards for this cardholder */
    this.myCards = new HashMap<>();
    this.password = password;
    this.registerDate = registerDate;
    this.addObserver(LogManager.getInstance());
  }

  /**
   * Create a new cardholder.
   *
   * @param name the name of this cardholder account.
   * @param email the email of this cardholder account.
   * @return an instance of a new Cardholder.
   */
  public static Cardholder getInstance(
      String name, String email, String password, Calendar registerDate) {
    return new Cardholder(name, email, password, registerDate);
  }

  public String getEmail() {
    return email;
  }

  /**
   * Add card in this cardholder account.
   *
   * @param card the card that cardholder want to store.
   */
  void storeMyCard(Card card) {
    this.myCards.put(card.getCardId(), card); // use cardID as key
    setChanged();
    notifyObservers("Cardholder " + name + " Got New Card " + card.getCardId() + ".");
    /* serialize cardholders if a cardholder created new card */
    SerializeManager.getInstance().writeObject();
  }

  /**
   * Get a specific card in all cards of this cardholder account.
   *
   * @param cardId the id of this card.
   * @return A card corresponding to this card id.
   */
  Card getCard(String cardId) {
    return myCards.get(cardId);
  }

  /** Get the name of this cardholder. */
  public String getName() {
    return this.name;
  }

  /**
   * Change the name of the cardholder account.
   *
   * @param newName new name for this cardholder account.
   */
  void setName(String newName) {
    String previousName = name;
    this.name = newName;
    this.setChanged();
    this.notifyObservers("Cardholder " + previousName + " Change Name To " + newName);
    /* serialize cardholders if a cardholder changed name */
    SerializeManager.getInstance().writeObject();
  }

  /**
   * Verify whether the input is the same as this cardholder's password.
   *
   * @return whether the input is the same as this cardholder's password.
   */
  public boolean verifyPassword(String input) {
    return this.password.equals(input);
  }

  /**
   * Remove a specific card from card bag of this cardholder account.
   *
   * @param cardId the Id of this card.
   */
  void removeFromMyCards(String cardId) {
    this.myCards.remove(cardId);
    setChanged();
    notifyObservers("Cardholder " + name + " Removed Card " + cardId + " From Card Bag.");
    /* serialize cardholders if a cardholder removed card */
    SerializeManager.getInstance().writeObject();
  }

  /**
   * Get all cards of this cardholder account.
   *
   * @return A HashMap of all cards, use cardId as key.
   */
  public HashMap<String, Card> getMyCards() {
    return this.myCards;
  }

  /**
   * Tell cardholder the information about this account.
   *
   * @return A string representing this cardholder.
   */
  @Override
  public String toString() {
    return "Cardholder Name: "
        + this.name
        + ", CardholderEmail: "
        + this.email
        + ", Register Date: "
        + registerDateToString();
  }

  /**
   * Get a String representing cardholder register date.
   *
   * @return A String representing register date of this cardholder.
   */
  public String registerDateToString() {
    int month = this.registerDate.get(Calendar.MONTH) + 1;
    return this.registerDate.get(Calendar.YEAR)
        + "-"
        + month
        + "-"
        + this.registerDate.get(Calendar.DATE);
  }
}
