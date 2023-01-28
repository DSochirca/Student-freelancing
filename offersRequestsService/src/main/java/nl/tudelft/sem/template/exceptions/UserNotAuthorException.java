package nl.tudelft.sem.template.exceptions;

public class UserNotAuthorException extends Exception {

    public static final long serialVersionUID = 4322743;

    public UserNotAuthorException(String user) {
        super("User with id " + user + " is not the author of this offer");
    }
}
