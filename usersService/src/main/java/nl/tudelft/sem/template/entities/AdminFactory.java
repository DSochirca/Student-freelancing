package nl.tudelft.sem.template.entities;

import nl.tudelft.sem.template.enums.Role;

public class AdminFactory implements UserFactory {
    @Override
    public User createUser(String username, String password) {
        return new Admin(username, password, Role.ADMIN);
    }
}
