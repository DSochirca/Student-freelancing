package nl.tudelft.sem.template.exceptions;

public class ContractNotExpiredException extends RuntimeException {
    public static final long serialVersionUID = 10;

    public ContractNotExpiredException() {
    }

    public ContractNotExpiredException(String message) {
        super(message);
    }
}
