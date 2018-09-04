package card;

import exception.NegativeBalanceException;
import exception.NoSuchCardException;
import exception.RemovedCardTapInException;
import exception.SuspendedCardTapInException;
import java.io.Serializable;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Observable;
import log.LogManager;
import ride.Ride;
import ride.RideRecord;
import serialize.SerializeManager;

/**
 * Represents a card manager in this transit system. A card manager can store all cards in this
 * transit system and get a card given the ID of card. A card manager manages tap in and tap out
 * features in this transit system as well as balance of cards.
 */
public class CardManager extends Observable implements Serializable {

  /* A card pool to store cards by their ID. */
  private final HashMap<String, Card> cardPool;
  /* Total number of cards created in this transit system, including suspended card and removed
   * card. Total number fo card is used to generate cardId. */
  private int numOfCard;

  /* Create a new CardManager. */
  public CardManager() {
    this.cardPool = new HashMap<>(); // create a new card pool to store cards by their ID.
    this.addObserver(LogManager.getInstance());
  }

  /**
   * Create a new card. Increment total number of cards created in this transit system by 1.
   *
   * @return the new card.
   */
  public Card createNewCard() {
    numOfCard++; // increase total number of cards in this transit system by 1
    Card newCard =
        Card.getInstance(
            Integer.toString(numOfCard)); // use the current total number of cards created as cardId
    this.cardPool.put(newCard.getCardId(), newCard); // store this card in card pool
    setChanged();
    notifyObservers("New Card " + newCard.getCardId() + " Successfully Stored In Card Pool!");
    /* serialize cards if new card is created */
    SerializeManager.getInstance().writeObject();
    return newCard;
  }

  /**
   * Add balance to the card with specific amount of money. If card manager cannot find
   * corresponding card of given cardId, tell the user that the cardId is invalid. The user can only
   * add $10, $20, $50 to a card. Otherwise, tell the user the amount is invalid.
   *
   * @param cardId id of this card.
   * @param amount how much money cardholder want to add.
   */
  public void addBalance(String cardId, double amount) throws NoSuchCardException {
    Card card = checkCard(cardId);
    card.addBalance(amount);
  }

  /**
   * Use a card to tap in. Tell the user if this card manager cannot find the card corresponding to
   * given ID. Prevent the user from entering stop/station if the balance is negative or the state
   * of this card is suspended or removed. Otherwise, add a new ride with given location and time to
   * ride records of this card.
   *
   * @param cardId id of card being used to tap in.
   * @param location the Station/Stop that the tap in is occurring at.
   * @param time the time that the tap in occurred.
   * @return the card that was used to tap in.
   */
  public Card tapIn(String cardId, String location, Calendar time, Ride.TransitType transitType)
      throws NoSuchCardException, NegativeBalanceException, SuspendedCardTapInException,
          RemovedCardTapInException {
    Card card = checkCard(cardId);
    if (card.getBalance() <= 0) { // this card has negative balance
      throw new NegativeBalanceException();
    } else if (card.getState() == Card.State.SUSPENDED) { // this card is suspended
      throw new SuspendedCardTapInException();
    } else if (card.getState() == Card.State.REMOVED) { // this card is removed
      throw new RemovedCardTapInException();
    } else {
      Ride newRide = Ride.getInstance(time, location, transitType);
      card.recordRide(newRide); // add a new ride to ride records of this card
      setChanged();
      notifyObservers(
          "Card "
              + cardId
              + " Tapped In At "
              + location
              + newRide.timeToString(newRide.getTapInTime()));
      /* serialize cards if a card tap in this transit system */
      SerializeManager.getInstance().writeObject();
      /*return this card for fare manager to check whether should charge or not and the amount to
       * charge if needed.*/
      return this.cardPool.get(cardId);
    }
  }

  /**
   * Use a card to tap out. Tell the user if this card manager cannot find the card corresponding to
   * the given ID.
   *
   * @param cardId id of card being used to tap out.
   * @param location the Station/Stop that the tap out is occurring at.
   * @param time the time that the tap out occurred.
   * @return the card that was used to tap out.
   */
  public Card tapOut(String cardId, String location, Calendar time, Ride.TransitType transitType)
      throws NoSuchCardException {
    Card card = checkCard(cardId);
    RideRecord rides = card.getRideRecords();
    Ride ride = rides.getLatestRide();
    /* a ride is normally generated when tapped in. add a new ride if there is a missed tap in,
     * has 2 sub cases */
    if (ride == null || ride.getTapOutLocation() != null) {
      /* this ride is the first ride or there are previous ride records of this card 8/
      /* set tap in location to "(Missed Tap In)" as a notice */
      Ride rideMissedTapIn = Ride.getInstance(null, "(Missed Tap In)", transitType);
      rides.add(rideMissedTapIn); // add this ride to ride records of this card
    }
    ride = rides.getLatestRide();
    ride.setTapOutLocation(location); // record tap out location of this ride
    ride.setTapOutTime(time); // record tap out time of this ride
    setChanged();
    notifyObservers(
        "Card " + cardId + " Tapped Out At " + location + ride.timeToString(ride.getTapOutTime()));
    /* serialize cards if a card tap out this transit system */
    SerializeManager.getInstance().writeObject();
    /* return this card for fare manager to check whether should charge or not and the amount to
     * charge if needed. */
    return card;
  }

  /**
   * Check whether or not this card is in the system.
   *
   * @param cardId the card id to search the system for.
   */
  public Card checkCard(String cardId) throws NoSuchCardException {
    Card card = this.cardPool.get(cardId);
    if (card == null) {
      setChanged();
      notifyObservers("Failed To Find Card: " + cardId + "!");
      throw new NoSuchCardException();
    }
    return card;
  }

  /**
   * Get a container storing all cards in this transit system.
   *
   * @return A HashMap of all cards, using ID of card as key.
   */
  public HashMap<String, Card> getCardPool() {
    return this.cardPool;
  }
}
