package nl.tudelft.sem.template.exceptions;

public class UserServiceUnvanvailableException extends Exception {
    private static final long serialVersionUID = 3L;

    public UserServiceUnvanvailableException() {
    }

    public UserServiceUnvanvailableException(String message) {
        super(message);
    }
}
