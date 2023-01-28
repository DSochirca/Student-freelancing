package nl.tudelft.sem.template.repositories;

import nl.tudelft.sem.template.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, String> {

    boolean existsByUsername(String username);
}
