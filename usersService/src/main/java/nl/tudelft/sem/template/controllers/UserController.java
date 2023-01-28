package nl.tudelft.sem.template.controllers;

import java.util.Optional;
import nl.tudelft.sem.template.domain.dtos.Response;
import nl.tudelft.sem.template.domain.dtos.UserCreateRequest;
import nl.tudelft.sem.template.domain.dtos.UserLoginRequest;
import nl.tudelft.sem.template.domain.dtos.UserLoginResponse;
import nl.tudelft.sem.template.entities.User;
import nl.tudelft.sem.template.enums.Role;
import nl.tudelft.sem.template.exceptions.UserAlreadyExists;
import nl.tudelft.sem.template.exceptions.UserNotFound;
import nl.tudelft.sem.template.services.UserControllerHelperService;
import nl.tudelft.sem.template.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {

    @Autowired
    private transient UserService userService;

    @Autowired
    private transient UserControllerHelperService userControllerHelperService;

    private final transient String nameHeader = "x-user-name";
    private final transient String roleHeader = "x-user-role";

    /**
     * Get a user by id.
     *
     * @param username User's username.
     * @return 200 OK with the user entity if the user is found,
     *          else 404 NOT FOUND.
     */
    @GetMapping("/{username}")
    public ResponseEntity<Response<User>> getUser(@PathVariable String username) {
        try {
            User user = userService.getUserOrRaise(username);
            return UserControllerHelperService.createUserResponse(user, HttpStatus.OK);
        } catch (UserNotFound e) {
            return UserControllerHelperService.createErrorResponse(
                    e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    private User getUser(UserCreateRequest userCreateRequest) {
        return userControllerHelperService.createUserFromRequest(userCreateRequest);
    }

    /**
     * Create a new user.
     *
     * @param userRequest User to create
     * @return 201 CREATED if the user is created,
     *         403 FORBIDDEN if callee is not an admin,
     *         409 CONFLICT if the user already exists,
     *         else 400 BAD REQUEST.
     */
    @PostMapping("/")
    public ResponseEntity<Response<User>> createUser(
            @RequestBody UserCreateRequest userRequest,
            @RequestHeader(roleHeader) String userRole
    ) {
        if (!UserControllerHelperService.isAdmin(userRole)) {
            return UserControllerHelperService.createErrorResponse(
                    "Only admins can create users", HttpStatus.FORBIDDEN);
        }
        try {
            User user = getUser(userRequest);
            User createdUser = userService.createUser(user);
            return UserControllerHelperService.createUserResponse(createdUser, HttpStatus.CREATED);
        } catch (UserAlreadyExists e) {
            return UserControllerHelperService.createErrorResponse(
                    e.getMessage(), HttpStatus.CONFLICT);
        }
    }

    /**
     * Update a user.
     *
     * @param userRequest User to update.
     * @return 200 OK if the user is updated,
     *         403 FORBIDDEN if callee is not an admin and is trying to update another user,
     *         403 FORBIDDEN if callee is not an admin and is trying to change their role,
     *         404 NOT FOUND if the user is not found,
     *         else 400 BAD REQUEST.
     */
    @PutMapping("/")
    public ResponseEntity<Response<User>> updateUser(
            @RequestBody UserCreateRequest userRequest,
            @RequestHeader(nameHeader) String userName,
            @RequestHeader(roleHeader) String userRole

    ) {
        boolean isAdmin = UserControllerHelperService.isAdmin(userRole);
        // Only admins can update other users.
        if (!(isAdmin || userName.equals(userRequest.getUsername()))) {
            return UserControllerHelperService.createErrorResponse(
                    "You can only update your own account.",
                    HttpStatus.FORBIDDEN
            );
        }
        // A user can not change their own role.
        if (!isAdmin && userRequest.getRole() != null
                && !userRequest.getRole().equals(Role.valueOf(userRole))) {
            return UserControllerHelperService.createErrorResponse(
                    "You can not change your role.",
                    HttpStatus.FORBIDDEN
            );
        }
        try {
            User user = getUser(userRequest);
            User updatedUser = userService.updateUser(user);
            return UserControllerHelperService.createUserResponse(updatedUser, HttpStatus.OK);
        } catch (UserNotFound e) {
            return UserControllerHelperService.createErrorResponse(
                    e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Delete a user.
     *
     * @param username User's id.
     * @return 200 OK if the user is deleted,
     *         403 FORBIDDEN if callee is not an admin,
     *         404 NOT FOUND if the user is not found.
     */
    @DeleteMapping("/{username}")
    public ResponseEntity<Response<User>> deleteUser(
            @PathVariable String username,
            @RequestHeader(roleHeader) String userRole
    ) {
        if (!UserControllerHelperService.isAdmin(userRole)) {
            return UserControllerHelperService.createErrorResponse(
                    "Only admins can delete users", HttpStatus.FORBIDDEN);
        }
        try {
            userService.deleteUser(username);
            return UserControllerHelperService.createUserResponse(null, HttpStatus.OK);
        } catch (UserNotFound e) {
            return UserControllerHelperService.createErrorResponse(
                    e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Login a user.
     *
     * @param user User ID and password to login
     * @return 200 OK with JWT token if user is logged in,
     *          else 401 UNAUTHORIZED
     */
    @PostMapping("/login")
    public ResponseEntity<Response<UserLoginResponse>> login(@RequestBody UserLoginRequest user) {
        Optional<User> u = userService.getUser(user.getUsername());
        if (u.isPresent()) {
            String token = userService.login(u.get(), user.getPassword());
            if (token != null) {
                return UserControllerHelperService.createLoginResponse(token);
            }
        }
        return UserControllerHelperService.createInvalidLoginResponse();
    }
}
