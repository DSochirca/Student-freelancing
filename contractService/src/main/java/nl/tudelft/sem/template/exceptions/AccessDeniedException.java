package nl.tudelft.sem.template.exceptions;

public class AccessDeniedException extends Exception {
    public static final long serialVersionUID = 1234571;

    public AccessDeniedException() {
        super("Access denied on this resource.");
    }

    public AccessDeniedException(String message) {
        super(message);
    }
}
