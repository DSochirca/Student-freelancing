package nl.tudelft.sem.template.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import java.util.Optional;
import logger.FileLogger;
import nl.tudelft.sem.template.entities.StudentFactory;
import nl.tudelft.sem.template.entities.User;
import nl.tudelft.sem.template.exceptions.UserAlreadyExists;
import nl.tudelft.sem.template.exceptions.UserNotFound;
import nl.tudelft.sem.template.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@SpringBootTest
@AutoConfigureMockMvc
public class UserServiceTest {

    @Autowired
    private transient UserService userService;

    @Autowired
    private transient AuthService authService;

    @MockBean
    private transient UserRepository userRepository;

    @MockBean
    private transient FileLogger fileLogger;

    private transient User user;

    @BeforeEach
    void setUp() {
        user = new StudentFactory().createUser("testing", "testPass");
    }

    @Test
    void deleteUserTest() {
        Mockito.doNothing()
                .when(userRepository).deleteById(user.getUsername());
        Mockito.when(userRepository.findById(user.getUsername()))
                .thenReturn(Optional.ofNullable(user));
        try {
            userService.deleteUser(user.getUsername());
        } catch (UserNotFound e) {
            e.printStackTrace();
        }

        verify(userRepository, times(1)).deleteById(user.getUsername());
    }

    @Test
    void getUserTest() {
        Mockito.when(userRepository.findById(user.getUsername()))
                .thenReturn(Optional.of(user));

        Optional<User> res = Optional.of(user);

        assertEquals(res, userService.getUser(user.getUsername()));
    }

    @Test
    void getUserOrRaiseTestPass() throws UserNotFound {
        Mockito.when(userRepository.findById("testing"))
                .thenReturn(Optional.ofNullable(user));

        assertEquals(user, userService.getUserOrRaise(user.getUsername()));
    }

    @Test
    void getUserOrRaiseTestError() {
        Mockito.when(userRepository.findById(user.getUsername()))
                .thenReturn(Optional.empty());

        String errorMessage = "Could not find user with ID testing";
        UserNotFound exception = assertThrows(UserNotFound.class,
                () -> userService.getUserOrRaise(user.getUsername()));

        assertEquals(errorMessage, exception.getMessage());
    }

    @Test
    void updateUserTestPass() throws UserNotFound {
        Mockito.when(userRepository.existsByUsername(any()))
                .thenReturn(true);

        Mockito.when(userRepository.save(user))
                .thenReturn(user);

        assertEquals(user, userService.updateUser(user));
    }

    @Test
    void updateUserTestError() {
        Mockito.when(userRepository.existsByUsername(any()))
                .thenReturn(false);

        String errorMessage = "Could not find user with ID testing";
        UserNotFound exception = assertThrows(UserNotFound.class,
                () -> userService.updateUser(user));

        assertEquals(errorMessage, exception.getMessage());
    }

    @Test
    void createUserTestPass() throws UserAlreadyExists {
        Mockito.when(userRepository.existsByUsername(any()))
                .thenReturn(false);

        Mockito.when(userRepository.save(user))
                .thenReturn(user);

        assertEquals(user, userService.createUser(user));
    }

    @Test
    void createUserTestError() {
        Mockito.when(userRepository.existsByUsername(any()))
                .thenReturn(true);

        String errorMessage = "User with id testing already exists";
        UserAlreadyExists exception = assertThrows(UserAlreadyExists.class,
                () -> userService.createUser(user));

        assertEquals(errorMessage, exception.getMessage());
    }
}
