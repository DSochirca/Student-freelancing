package nl.tudelft.sem.template.exceptions;

public class InvalidContractException extends Exception {
    public static final long serialVersionUID = 1234572;

    public InvalidContractException() {
        super("One or more contract parameters are invalid.");
    }

    public InvalidContractException(String message) {
        super(message);
    }
}
