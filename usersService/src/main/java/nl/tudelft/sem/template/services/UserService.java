package nl.tudelft.sem.template.services;

import java.util.Optional;
import logger.FileLogger;
import nl.tudelft.sem.template.domain.dtos.UserCreateRequest;
import nl.tudelft.sem.template.entities.Admin;
import nl.tudelft.sem.template.entities.User;
import nl.tudelft.sem.template.exceptions.UserAlreadyExists;
import nl.tudelft.sem.template.exceptions.UserNotFound;
import nl.tudelft.sem.template.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private transient UserRepository userRepository;

    @Autowired
    private transient AuthService authService;

    @Autowired
    private transient FileLogger logger;

    /** Get user by their id.
     *
     * @param username User's username.
     * @return User object.
     */
    public Optional<User> getUser(String username) {
        return userRepository.findById(username);
    }

    /**
     * Get user by their id, throws UserNotFound if user is not found.
     *
     * @param username - the username.
     * @return User object.
     * @throws UserNotFound if user is not found.
     */
    public User getUserOrRaise(String username) throws UserNotFound {
        Optional<User> u = userRepository.findById(username);
        if (u.isPresent()) {
            return u.get();
        }
        throw new UserNotFound(username);
    }

    /**
     * Verify if the password matches the user's password.
     *
     * @param user user to verify password for.
     * @param password plaintext password to verify.
     * @return true if password matches, false otherwise.
     */
    public String login(User user, String password) {
        if (authService.verifyPassword(user, password)) {
            return authService.generateJwtToken(user);
        }
        return null;
    }


    /**
     * Create a new user.
     *
     * @param user User to create.
     * @return User object.
     * @throws UserAlreadyExists if a user with same id already exists.
     */
    public User createUser(User user) throws UserAlreadyExists {
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new UserAlreadyExists(user);
        }
        logger.log(user.getRole() + " has been created with username " + user.getUsername());
        user.setPassword(authService.hashPassword(user.getPassword()));
        return userRepository.save(user);
    }

    /**
     * Update a user.
     *
     * @param user User to update.
     * @return User object.
     * @throws UserNotFound if user is not found.
     */
    public User updateUser(User user) throws UserNotFound {
        if (userRepository.existsByUsername(user.getUsername())) {
            user.setPassword(authService.hashPassword(user.getPassword()));
            userRepository.deleteById(user.getUsername());
            return userRepository.save(user);
        }
        throw new UserNotFound(user.getUsername());
    }

    /**
     * Deletes a user.
     *
     * @param username - the username of the User.
     * @throws UserNotFound - is thrown if such a User doesn't exist.
     */
    public void deleteUser(String username) throws UserNotFound {
        getUserOrRaise(username);
        userRepository.deleteById(username);
        logger.log("User " + username + " has been deleted");
    }

    /**
     *  Creates a default admin account on user service startup.
     *  If the admin account already exists, it will not be created.
     */
    @EventListener(ApplicationReadyEvent.class)
    public void createAdminAccount() {
        Admin adminUser = new Admin("admin", authService.getAdminPassword());
        try {
            createUser(adminUser);
            logger.log("Admin account created");
        } catch (UserAlreadyExists userAlreadyExists) {
            logger.log("Admin account already exists");
        }
    }
}
