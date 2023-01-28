package nl.tudelft.sem.template.exceptions;

public class InactiveContractException extends Exception {
    public static final long serialVersionUID = 1234570;

    public InactiveContractException() {
        super("This contract has expired or was terminated.");
    }
}