package exception;

/**
 * Thrown if a cardholder tried to tap in with a card with negative balance. The transit system will
 * stop the cardholder to enter the transit system in this case.
 */
public class NegativeBalanceException extends Exception {}
