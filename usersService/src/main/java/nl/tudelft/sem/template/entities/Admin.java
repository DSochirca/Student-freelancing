package nl.tudelft.sem.template.entities;

import javax.persistence.Entity;
import lombok.NoArgsConstructor;
import nl.tudelft.sem.template.enums.Role;

@Entity
@NoArgsConstructor
public class Admin extends User {
    Admin(String username, String password, Role role) {
        super(username, password, role);
    }

    public Admin(String username, String password) {
        super(username, password, Role.ADMIN);
    }
}