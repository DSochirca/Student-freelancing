package nl.tudelft.sem.template.exceptions;

public class UserServiceUnavailableException extends RuntimeException {
    public static final long serialVersionUID = 5;

    public UserServiceUnavailableException() {
    }

    public UserServiceUnavailableException(String message) {
        super(message);
    }
}
