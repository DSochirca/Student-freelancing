package nl.tudelft.sem.template.exceptions;

public class InvalidRoleException extends RuntimeException {
    public static final long serialVersionUID = 4;

    public InvalidRoleException() {
    }

    public InvalidRoleException(String message) {
        super(message);
    }
}
