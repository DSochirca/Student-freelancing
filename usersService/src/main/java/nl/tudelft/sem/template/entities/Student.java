package nl.tudelft.sem.template.entities;

import javax.persistence.Entity;
import lombok.NoArgsConstructor;
import nl.tudelft.sem.template.enums.Role;

@Entity
@NoArgsConstructor
public class Student extends User {
    Student(String username, String password, Role role) {
        super(username, password, role);
    }
}
