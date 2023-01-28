package nl.tudelft.sem.template.exceptions;

public class InvalidFeedbackDetailsException extends RuntimeException {
    public static final long serialVersionUID = 7;

    public InvalidFeedbackDetailsException() {
    }

    public InvalidFeedbackDetailsException(String message) {
        super(message);
    }
}
