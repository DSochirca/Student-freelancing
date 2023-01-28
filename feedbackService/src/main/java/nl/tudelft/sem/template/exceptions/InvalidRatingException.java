package nl.tudelft.sem.template.exceptions;

public class InvalidRatingException extends RuntimeException {
    public static final long serialVersionUID = 3;

    public InvalidRatingException() {
    }

    public InvalidRatingException(String message) {
        super(message);
    }
}
