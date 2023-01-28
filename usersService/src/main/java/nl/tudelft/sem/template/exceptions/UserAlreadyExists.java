package nl.tudelft.sem.template.exceptions;

import nl.tudelft.sem.template.entities.User;

public class UserAlreadyExists extends Exception {

    public static final long serialVersionUID = 4328743;

    public UserAlreadyExists(User user) {
        super("User with id " + user.getUsername() + " already exists");
    }

}
