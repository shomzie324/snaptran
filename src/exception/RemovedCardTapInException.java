package exception;

/**
 * Thrown if a cardholder tried to tap in with a card in removed state. A removed card cannot enter
 * the transit system. The transit system will stop the cardholder to enter the transit system in
 * this case.
 */
public class RemovedCardTapInException extends Exception {}
