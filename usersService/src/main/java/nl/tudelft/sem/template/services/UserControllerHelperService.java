package nl.tudelft.sem.template.services;

import nl.tudelft.sem.template.domain.dtos.Response;
import nl.tudelft.sem.template.domain.dtos.UserCreateRequest;
import nl.tudelft.sem.template.domain.dtos.UserLoginResponse;
import nl.tudelft.sem.template.entities.CompanyFactory;
import nl.tudelft.sem.template.entities.StudentFactory;
import nl.tudelft.sem.template.entities.User;
import nl.tudelft.sem.template.enums.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class UserControllerHelperService {
    @Autowired
    private transient StudentFactory studentFactory;

    @Autowired
    private transient CompanyFactory companyFactory;

    public static ResponseEntity<Response<User>> createUserResponse(User user, HttpStatus status) {
        return new ResponseEntity<>(new Response<>(user, null), status);
    }

    public static ResponseEntity<Response<UserLoginResponse>> createLoginResponse(String token) {
        return new ResponseEntity<>(new Response<>(
                new UserLoginResponse(token), null), HttpStatus.OK);
    }

    public static ResponseEntity<Response<UserLoginResponse>> createInvalidLoginResponse() {
        return new ResponseEntity<>(new Response<>(new UserLoginResponse(null),
                "Invalid username or password."), HttpStatus.UNAUTHORIZED);
    }

    public static ResponseEntity<Response<User>> createErrorResponse(
            String error, HttpStatus status) {
        return new ResponseEntity<>(new Response<>(null, error), status);
    }

    public static Boolean isAdmin(String userRoleHeader) {
        return Role.ADMIN.toString().equals(userRoleHeader);
    }

    /**
     * Create a user based on the given request.
     *
     * @param userCreateRequest The request containing the user information.
     * @return The created user.
     */
    public User createUserFromRequest(UserCreateRequest userCreateRequest) {
        switch (userCreateRequest.getRole()) {
            case STUDENT:
                return studentFactory.createUser(userCreateRequest.getUsername(),
                        userCreateRequest.getPassword());
            case COMPANY:
                return companyFactory.createUser(userCreateRequest.getUsername(),
                        userCreateRequest.getPassword());
            default:
                throw new IllegalArgumentException("Invalid role " + userCreateRequest.getRole());
        }
    }
}
