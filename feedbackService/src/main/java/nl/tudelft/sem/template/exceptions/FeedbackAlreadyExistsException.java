package nl.tudelft.sem.template.exceptions;

public class FeedbackAlreadyExistsException extends RuntimeException {
    public static final long serialVersionUID = 41;

    public FeedbackAlreadyExistsException() {
    }

    public FeedbackAlreadyExistsException(String message) {
        super(message);
    }
}
