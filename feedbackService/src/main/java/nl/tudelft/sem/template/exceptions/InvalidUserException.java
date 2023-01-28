package nl.tudelft.sem.template.exceptions;

public class InvalidUserException extends RuntimeException {
    public static final long serialVersionUID = 6;

    public InvalidUserException() {
    }

    public InvalidUserException(String message) {
        super(message);
    }
}
