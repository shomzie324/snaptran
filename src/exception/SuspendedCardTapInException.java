package exception;

/**
 * Thrown if a cardholder tried to tap in with a card in suspended state. A suspended card cannot
 * enter the transit system. The transit system will stop the cardholder to enter the transit system
 * in this case.
 */
public class SuspendedCardTapInException extends Exception {}
