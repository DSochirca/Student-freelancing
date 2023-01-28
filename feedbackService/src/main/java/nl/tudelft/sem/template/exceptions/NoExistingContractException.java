package nl.tudelft.sem.template.exceptions;

public class NoExistingContractException extends RuntimeException {
    public static final long serialVersionUID = 9;

    public NoExistingContractException() {
    }

    public NoExistingContractException(String message) {
        super(message);
    }
}
