package exception;

/**
 * Thrown if a cardholder tried to request an action on a card that does not recorded in this
 * transit system. The transit system will stop the cardholder to enter the transit system in this
 * case.
 */
public class NoSuchCardException extends Exception {}
