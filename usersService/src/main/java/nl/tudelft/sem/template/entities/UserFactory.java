package nl.tudelft.sem.template.entities;

public interface UserFactory {
    /**
     * Creates an instance of <code>User</code>.
     *
     * @param username The username
     * @param password The password
     * @return A new User.
     */
    User createUser(String username, String password);
}
