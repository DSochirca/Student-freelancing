package nl.tudelft.sem.template.exceptions;

public class LowRatingException extends Exception {

    public static long serialVersionUID = 5L;

    public LowRatingException(String action, double requiredRating) {
        super("Action '" + action + "' requires a rating of at least " + requiredRating);
    }
}
