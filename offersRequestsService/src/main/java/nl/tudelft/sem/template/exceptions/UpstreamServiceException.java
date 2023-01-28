package nl.tudelft.sem.template.exceptions;

public class UpstreamServiceException extends Exception {

    public static long serialVersionUID = 5L;

    public UpstreamServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
