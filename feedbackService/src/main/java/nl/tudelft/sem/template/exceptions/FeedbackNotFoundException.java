package nl.tudelft.sem.template.exceptions;

public class FeedbackNotFoundException extends RuntimeException {
    public static final long serialVersionUID = 8;

    public FeedbackNotFoundException() {
    }

    public FeedbackNotFoundException(String message) {
        super(message);
    }
}
