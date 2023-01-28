package nl.tudelft.sem.template.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

import nl.tudelft.sem.template.domain.dtos.Response;
import nl.tudelft.sem.template.domain.dtos.UserCreateRequest;
import nl.tudelft.sem.template.domain.dtos.UserLoginRequest;
import nl.tudelft.sem.template.domain.dtos.UserLoginResponse;
import nl.tudelft.sem.template.entities.StudentFactory;
import nl.tudelft.sem.template.entities.User;
import nl.tudelft.sem.template.enums.Role;
import nl.tudelft.sem.template.exceptions.UserAlreadyExists;
import nl.tudelft.sem.template.exceptions.UserNotFound;
import nl.tudelft.sem.template.services.UserControllerHelperService;
import nl.tudelft.sem.template.services.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@SpringBootTest
@AutoConfigureMockMvc
public class UserControllerTests {

    @Autowired
    private transient UserController userController;

    @MockBean
    private transient UserService userService;

    private transient User user;
    private transient Response<User> response;

    @BeforeEach
    void setUp() {
        user = new StudentFactory().createUser("testing", "testPass");
        response = new Response<>(user, null);
    }

    @Test
    void getUserTest() throws UserNotFound {
        Mockito.when(userService.getUserOrRaise(any()))
                .thenReturn(user);

        assertEquals(response, userController.getUser(user.getUsername()).getBody());
    }

    @Test
    void getUserTestError() throws UserNotFound {
        Mockito.when(userService.getUserOrRaise(any()))
                .thenThrow(new UserNotFound(user.getUsername()));

        response.setErrorMessage("Could not find user with ID testing");
        response.setData(null);
        ResponseEntity<Response<User>> entity =
                new ResponseEntity<>(response, HttpStatus.NOT_FOUND);

        assertEquals(entity, userController.getUser("testing"));
    }

    @Test
    void createUserTest() throws UserAlreadyExists {
        Mockito.when(userService.createUser(user))
                .thenReturn(user);

        ResponseEntity<Response<User>> entity =
                new ResponseEntity<>(response, HttpStatus.CREATED);

        UserCreateRequest userRequest =
            new UserCreateRequest(user.getUsername(), user.getPassword(), user.getRole());
        assertEquals(entity, userController.createUser(userRequest, Role.ADMIN.toString()));
    }

    @Test
    void createUserTestErrorAlreadyExists() throws UserAlreadyExists {
        String errorMessage = "User with id testing already exists";
        Mockito.when(userService.createUser(user))
                .thenThrow(new UserAlreadyExists(user));

        response.setData(null);
        response.setErrorMessage(errorMessage);

        ResponseEntity<Response<User>> entity =
                new ResponseEntity<>(response, HttpStatus.CONFLICT);

        UserCreateRequest userRequest =
            new UserCreateRequest(user.getUsername(), user.getPassword(), user.getRole());
        assertEquals(entity, userController.createUser(userRequest, Role.ADMIN.toString()));
    }

    @Test
    void createUserForbiddenNotAdmin() {
        String errorMessage = "Only admins can create users";
        response.setData(null);
        response.setErrorMessage(errorMessage);

        ResponseEntity<Response<User>> entity =
                new ResponseEntity<>(response, HttpStatus.FORBIDDEN);

        UserCreateRequest userRequest =
            new UserCreateRequest(user.getUsername(), user.getPassword(), user.getRole());
        assertEquals(entity, userController.createUser(userRequest, Role.STUDENT.toString()));
    }


    @Test
    void deleteUserTest() throws UserNotFound {
        Mockito.doNothing()
                .when(userService)
                .deleteUser(user.getUsername());

        ResponseEntity<Response<User>> entity = UserControllerHelperService.createUserResponse(
                null, HttpStatus.OK);
        assertEquals(entity, userController.deleteUser(user.getUsername(), Role.ADMIN.toString()));
    }

    @Test
    void deleteUserTestError() throws UserNotFound {
        doThrow(new UserNotFound(user.getUsername()))
                .when(userService)
                .deleteUser(user.getUsername());

        ResponseEntity<Response<User>> entity = UserControllerHelperService.createErrorResponse(
                "Could not find user with ID testing",
                HttpStatus.NOT_FOUND
        );

        assertEquals(entity, userController.deleteUser(user.getUsername(), Role.ADMIN.toString()));
    }

    @Test
    void deleteUserForbiddenNotAdmin() {
        ResponseEntity<Response<User>> entity = UserControllerHelperService.createErrorResponse(
                "Only admins can delete users",
                HttpStatus.FORBIDDEN
        );
        assertEquals(entity, userController.deleteUser("test123", Role.STUDENT.toString()));
    }

    @Test
    void updateUserTestAdmin() throws UserNotFound {
        Mockito.when(userService.updateUser(user))
                .thenReturn(user);

        ResponseEntity<Response<User>> entity =
                new ResponseEntity<>(response, HttpStatus.OK);

        UserCreateRequest userRequest =
            new UserCreateRequest(user.getUsername(), user.getPassword(), user.getRole());

        assertEquals(entity, userController
                .updateUser(userRequest, "admin", Role.ADMIN.toString()));
    }

    @Test
    void updateUserTestOwnUser() throws UserNotFound {
        Mockito.when(userService.updateUser(user))
                .thenReturn(user);

        ResponseEntity<Response<User>> entity =
                new ResponseEntity<>(response, HttpStatus.OK);

        UserCreateRequest userRequest =
                new UserCreateRequest(user.getUsername(), user.getPassword(), user.getRole());
        assertEquals(entity, userController
                .updateUser(userRequest, user.getUsername(), user.getRole().toString()));
    }

    @Test
    void updateUserTestUserNotFoundTest() throws UserNotFound {
        String errorMessage = "Could not find user with ID testing";
        Mockito.when(userService.updateUser(user))
                .thenThrow(new UserNotFound(user.getUsername()));

        response.setData(null);
        response.setErrorMessage(errorMessage);

        ResponseEntity<Response<User>> entity =
                new ResponseEntity<>(response, HttpStatus.NOT_FOUND);

        UserCreateRequest userRequest =
            new UserCreateRequest(user.getUsername(), user.getPassword(), user.getRole());
        assertEquals(entity, userController
                .updateUser(userRequest, "admin2", Role.ADMIN.toString()));
    }

    @Test
    void updateUserTestForbiddenNotAdmin() {
        String errorMessage = "You can only update your own account.";
        response.setData(null);
        response.setErrorMessage(errorMessage);

        ResponseEntity<Response<User>> entity =
                new ResponseEntity<>(response, HttpStatus.FORBIDDEN);

        UserCreateRequest userRequest =
                new UserCreateRequest(user.getUsername(), user.getPassword(), user.getRole());
        assertEquals(entity, userController
                .updateUser(userRequest, "notAdmin", Role.STUDENT.toString()));
    }

    @Test
    void updateUserTestForbiddenUserRoleChange() {
        String errorMessage = "You can only update your own account.";
        response.setData(null);
        response.setErrorMessage(errorMessage);

        ResponseEntity<Response<User>> entity =
                new ResponseEntity<>(response, HttpStatus.FORBIDDEN);

        UserCreateRequest userRequest =
                new UserCreateRequest(user.getUsername(), user.getPassword(), Role.ADMIN);
        assertEquals(entity, userController
                .updateUser(userRequest, "notAdmin", Role.STUDENT.toString()));
    }

    @Test
    void loginUserTest() {
        Mockito.when(userService.getUser(user.getUsername()))
                .thenReturn(java.util.Optional.ofNullable(user));
        Mockito.when(userService.login(user, user.getPassword()))
                .thenReturn("generated_token_test");

        ResponseEntity<Response<UserLoginResponse>> loginResponse = userController.login(
                new UserLoginRequest(user.getUsername(), user.getPassword())
        );

        assertEquals(HttpStatus.OK, loginResponse.getStatusCode());
        assertNotNull(loginResponse.getBody());
        assertEquals("generated_token_test", loginResponse.getBody().getData().getToken());
    }

    @Test
    void loginUserTestPasswordIncorrect() {
        Mockito.when(userService.getUser(user.getUsername()))
                .thenReturn(java.util.Optional.ofNullable(user));
        Mockito.when(userService.login(user, user.getPassword()))
                .thenReturn("generated_token_test");

        ResponseEntity<Response<UserLoginResponse>> loginResponse = userController.login(
                new UserLoginRequest(user.getUsername(), "this_is_not_the_password")
        );

        assertEquals(HttpStatus.UNAUTHORIZED, loginResponse.getStatusCode());
    }

    @Test
    void loginUserTestUserNotFound() {
        Mockito.when(userService.getUser(any())).thenReturn(java.util.Optional.empty());

        ResponseEntity<Response<UserLoginResponse>> loginResponse = userController.login(
                new UserLoginRequest(user.getUsername(), user.getPassword())
        );

        assertEquals(HttpStatus.UNAUTHORIZED, loginResponse.getStatusCode());
    }
}
