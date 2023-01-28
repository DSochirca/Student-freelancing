package nl.tudelft.sem.template.exceptions;


public class UserNotFound extends Exception {

    public static final long serialVersionUID = 4328744;

    public UserNotFound(String userId) {
        super("Could not find user with ID " + userId);
    }
}
