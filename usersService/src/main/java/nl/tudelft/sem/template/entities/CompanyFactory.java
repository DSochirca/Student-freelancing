package nl.tudelft.sem.template.entities;

import nl.tudelft.sem.template.enums.Role;
import org.springframework.stereotype.Component;

@Component
public class CompanyFactory implements UserFactory {
    @Override
    public User createUser(String username, String password) {
        return new Company(username, password, Role.COMPANY);
    }
}
