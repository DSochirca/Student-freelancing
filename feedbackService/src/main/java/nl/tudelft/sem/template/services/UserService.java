package nl.tudelft.sem.template.services;

import nl.tudelft.sem.template.domain.dtos.responses.UserResponse;
import nl.tudelft.sem.template.domain.dtos.responses.UserResponseWrapper;
import nl.tudelft.sem.template.exceptions.UserServiceUnavailableException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Service
public class UserService {
    @Autowired
    private transient RestTemplate restTemplate;


    /**
     * Get the user with the given username.
     *
     * @param username user to be retrieved
     * @return the user with the given username or null if the user does not exist
     */
    public UserResponse getUser(String username) throws UserServiceUnavailableException {
        String userServiceUrl = "http://users-service/";
        try {
            UserResponseWrapper userResponse =  restTemplate
                    .getForObject(userServiceUrl + username, UserResponseWrapper.class);
            if (userResponse == null) {
                throw new UserServiceUnavailableException("User response was null");
            }
            if (userResponse.getErrorMessage() != null) {
                return null;
            }
            return userResponse.getData();
        } catch (RestClientException e) {
            if (e.getMessage() != null && e.getMessage().contains("Could not find user")) {
                return null;
            }
            throw new UserServiceUnavailableException(e.getMessage());
        }
    }


    /**
     * Check if a user with the given username exists.
     *
     * @param username user to be checked
     * @return true if the user exists, false otherwise
     */
    public boolean userExists(String username) {
        return getUser(username) != null;
    }
}
