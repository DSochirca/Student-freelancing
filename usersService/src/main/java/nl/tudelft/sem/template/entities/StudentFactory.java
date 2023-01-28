package nl.tudelft.sem.template.entities;

import nl.tudelft.sem.template.enums.Role;
import org.springframework.stereotype.Component;

@Component
public class StudentFactory implements UserFactory {
    @Override
    public User createUser(String username, String password) {
        return new Student(username, password, Role.STUDENT);
    }
}
