package cardholder;

import card.Card;
import exception.NoSuchCardException;
import exception.NoSuchCardholderException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Observable;
import log.LogManager;
import ride.Ride;
import ride.RideRecord.RideIterator;
import serialize.SerializeManager;

/**
 * Represent a cardholder manager with a cardholder pool storing all cardholders in this transit
 * system. A cardholder can add new cardholder to its card pool, pass a new card created by card
 * manager to its owner (to store in card bag), suspend or remove a card for a cardholder, view
 * balance or 3 recent trips of a card given cardholder email and cardID, change name of a
 * cardholder, view average month fare of a cardholder,
 */
public class CardholderManager extends Observable implements Serializable {

  private final HashMap<String, Cardholder> cardholderPool;

  /**
   * Create a new cardholder manager with a new cardholder pool to store all cardholders in this
   * transit system.
   */
  public CardholderManager() {
    this.cardholderPool = new HashMap<>();
    this.addObserver(LogManager.getInstance());
  }

  /**
   * Create a new cardholder account and store in cardholder pool if this user does not have a
   * cardholder account in this transit system, otherwise claim that this account already exists.
   *
   * @param cardholder A new Cardholder.
   */
  public void addNewCardholder(Cardholder cardholder) {
    if (this.cardholderPool.get(cardholder.getEmail()) == null) { // this user is a new cardholder
      this.cardholderPool.put(
          cardholder.getEmail(), cardholder); // store new cardholder in cardholder pool
      setChanged();
      notifyObservers("The account " + cardholder.getEmail() + " has been created!");
      /* serialize cardholders if a new cardholder is created */
      SerializeManager.getInstance().writeObject();
    } else { // this user already have a cardholder account
      setChanged();
      notifyObservers("The account " + cardholder.getEmail() + " already exists!");
    }
  }

  /**
   * Pass a new card to its cardholder.
   *
   * @param cardholderEmail the email address of the account.
   * @param card new card to pass to its cardholder.
   */
  public void passNewCard(String cardholderEmail, Card card) throws NoSuchCardholderException {
    Cardholder cardholder = checkCardholder(cardholderEmail);
    cardholder.storeMyCard(card); // store this card in cardholder's card bag
  }

  /**
   * Suspend a card in this cardholder account. The cardholder can decide to reactivate this card or
   * transfer balance to another card and remove this card.
   *
   * @param cardholderEmail the email address of this cardholder account.
   * @param cardId id of the card to suspend.
   */
  public void suspendCard(String cardholderEmail, String cardId)
      throws NoSuchCardholderException, NoSuchCardException {
    Cardholder cardholder = checkCardholder(cardholderEmail);
    Card card = checkCard(cardholder, cardId);
    card.setState(Card.State.SUSPENDED); // set state of this card to “suspended"
  }

  /**
   * Reactivate a card in the cardholder account.
   *
   * @param cardholderEmail the email address of the account.
   * @param cardId the id of the card.
   */
  public void reactivateCard(String cardholderEmail, String cardId)
      throws NoSuchCardholderException, NoSuchCardException {
    Cardholder cardholder = checkCardholder(cardholderEmail);
    Card card = checkCard(cardholder, cardId);
    card.setState(Card.State.ACTIVE);
  }

  /**
   * Cardholder can transfer the balance from a suspended card to another card. After transferred,
   * this suspended card will be removed.
   *
   * @param cardholderEmail the email address of the account.
   * @param cardId1 the id of the suspended card.
   * @param card2 the card that will be added balance.
   */
  public void transferBalance(String cardholderEmail, String cardId1, Card card2)
      throws NoSuchCardholderException, NoSuchCardException {
    Cardholder cardholder = checkCardholder(cardholderEmail);
    Card card1 = checkCard(cardholder, cardId1);
    double balance = card1.getBalance();
    card2.addBalance(balance);
    card1.deductBalance(balance);
    removeCard(cardholderEmail, cardId1);
    setChanged();
    notifyObservers(
        "Cardholder "
            + cardholderEmail
            + " Transfer Balance Of Card "
            + cardId1
            + " $"
            + balance
            + " To Card "
            + card2.getCardId()
            + ".");
  }

  /**
   * Remove the card from the cardholder account. The cardholder will not have access to removed
   * card.
   *
   * @param cardholderEmail the email address of the account.
   * @param cardId the id of the card.
   */
  public void removeCard(String cardholderEmail, String cardId)
      throws NoSuchCardholderException, NoSuchCardException {
    Cardholder cardholder = checkCardholder(cardholderEmail);
    Card card = checkCard(cardholder, cardId);
    card.setState(Card.State.REMOVED); // set state of this card to "removed"
    /*remove this card from cardholder's card bag, have no access to removed card any more*/
    cardholder.removeFromMyCards(cardId);
  }

  /**
   * Controller the balance of a specific card.
   *
   * @param cardholderEmail the email address of the account.
   * @param cardId the id of the card.
   */
  public String viewBalance(String cardholderEmail, String cardId)
      throws NoSuchCardholderException, NoSuchCardException {
    Cardholder cardholder = checkCardholder(cardholderEmail);
    Card card = checkCard(cardholder, cardId);
    setChanged();
    notifyObservers(
        "Cardholder With Email: " + cardholderEmail + " Viewed Balance Of Card " + cardId + ".");
    return String.valueOf(card.getBalance());
  }

  /**
   * Controller the state of a specific card.
   *
   * @param cardholderEmail the email address of the account.
   * @param cardId the id of the card.
   */
  public String viewState(String cardholderEmail, String cardId)
      throws NoSuchCardholderException, NoSuchCardException {
    Cardholder cardholder = checkCardholder(cardholderEmail);
    Card card = checkCard(cardholder, cardId);
    setChanged();
    notifyObservers(
        "Cardholder With Email: " + cardholderEmail + " Viewed State Of Card " + cardId + ".");
    return String.valueOf(card.getState());
  }

  /**
   * Controller the three recent trips of the card in the cardholder account.
   *
   * @param cardholderEmail the email address of the account.
   * @param cardId the id of the card.
   */
  public String viewRecentTrips(String cardholderEmail, String cardId, String reportPeriod)
      throws NoSuchCardholderException, NoSuchCardException {
    Cardholder cardholder = checkCardholder(cardholderEmail);
    Card card = checkCard(cardholder, cardId);
    setChanged();
    notifyObservers(
        "Cardholder With Email: "
            + cardholderEmail
            + " Viewed Recent Trips Of Card "
            + cardId
            + ".");
    return card.reportRecentTrips(reportPeriod);
  }

  /**
   * Change the name of the cardholder account.
   *
   * @param cardholderEmail the email address of the account.
   * @param name the new name of the account.
   */
  public void changeName(String cardholderEmail, String name) throws NoSuchCardholderException {
    Cardholder cardholder = checkCardholder(cardholderEmail);
    cardholder.setName(name);
  }

  /**
   * Get the average month fare of a cardholder account.
   *
   * @param cardholderEmail the email address of the account.
   */
  public String getAverageMonthFare(String cardholderEmail) throws NoSuchCardholderException {
    Cardholder cardholder = checkCardholder(cardholderEmail);
    double totalMonthFare = 0;
    int month = 0;
    ArrayList<Card> cards = new ArrayList<>(cardholder.getMyCards().values());
    for (Card card : cards) {
      RideIterator iterator = card.getRideRecords().monthlyIterator();
      while (iterator != null && iterator.hasNext()) {
        for (Ride ride : iterator.next()) {
          totalMonthFare += ride.getFare();
        }
        month += 1;
      }
    }
    setChanged();
    notifyObservers(
        "Cardholder With Email: " + cardholderEmail + " Checked Average Month Fare Of Cards.");
    return String.valueOf(totalMonthFare / month);
  }

  /**
   * Check whether or not this cardholder has at least one tap in.
   *
   * @param cardholderEmail the email address of the account.
   */
  public boolean hasRecord(String cardholderEmail) {
    boolean has = false;
    Cardholder cardholder = this.cardholderPool.get(cardholderEmail);
    if (!cardholder.getMyCards().isEmpty()) {
      ArrayList<Card> cards = new ArrayList<>(cardholder.getMyCards().values());
      for (Card card : cards) {
        if (!card.getRideRecords().getAllRides().isEmpty()) {
          has = true;
        }
      }
    }
    if (!has) {
      setChanged();
      notifyObservers(
          "Cardholder With Email: "
              + cardholderEmail
              + " Checked Average Month Fare Of Cards But Has No Record.");
    }
    return has;
  }

  /**
   * Check whether or not this cardholder is existed.
   *
   * @param cardholderEmail the email address of the account.
   */
  public Cardholder checkCardholder(String cardholderEmail) throws NoSuchCardholderException {
    Cardholder cardholder = cardholderPool.get(cardholderEmail);
    if (cardholder == null) {
      setChanged();
      notifyObservers("Failed To Find Cardholder With Email: " + cardholderEmail + " !");
      throw new NoSuchCardholderException();
    }
    return cardholder;
  }

  /**
   * Check whether or not this card is in the cardholder account.
   *
   * @param cardholder A cardholder we want to check.
   * @param cardId A id of a Card we want to check.
   */
  private Card checkCard(Cardholder cardholder, String cardId) throws NoSuchCardException {
    Card card = cardholder.getCard(cardId);
    if (card == null) {
      setChanged();
      notifyObservers("Failed To Find Card: " + cardId + "!");
      throw new NoSuchCardException();
    }
    return card;
  }

  /**
   * Get the cardholder pool of this cardholder manager which contains all cardholders in this
   * transit system.
   *
   * @return cardholder pool which contains all cardholders in this transit system.
   */
  public HashMap<String, Cardholder> getCardholderPool() {
    return cardholderPool;
  }
}
