package nl.tudelft.sem.template.exceptions;

public class InvalidChangeProposalException extends Exception {
    public static final long serialVersionUID = 1234568;

    public InvalidChangeProposalException() {
        super("Could not submit a change to the contract with the suggested parameters.");
    }

    public InvalidChangeProposalException(String message) {
        super(message);
    }
}
