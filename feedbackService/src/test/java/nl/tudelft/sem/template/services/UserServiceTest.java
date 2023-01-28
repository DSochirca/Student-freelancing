package nl.tudelft.sem.template.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import nl.tudelft.sem.template.domain.dtos.enums.UserRole;
import nl.tudelft.sem.template.domain.dtos.responses.UserResponse;
import nl.tudelft.sem.template.domain.dtos.responses.UserResponseWrapper;
import nl.tudelft.sem.template.exceptions.UserServiceUnavailableException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@SpringBootTest
@AutoConfigureMockMvc
public class UserServiceTest {
    @Autowired
    private transient UserService userService;

    @MockBean
    private transient RestTemplate restTemplate;

    private static final String userName = "TestUser";
    private static final String userServiceEndpoint = "http://users-service/";

    @Test
    void testGetUser() {
        UserResponse userResponse = new UserResponse(userName, "test2", UserRole.STUDENT);
        UserResponseWrapper userResponseWrapper = new UserResponseWrapper();
        userResponseWrapper.setData(userResponse);
        when(restTemplate.getForObject(userServiceEndpoint + userName, UserResponseWrapper.class))
                .thenReturn(userResponseWrapper);
        assertEquals(userResponse, userService.getUser(userName));
    }

    @Test
    void testGetUserNoResponse() {
        when(restTemplate.getForObject(userServiceEndpoint + userName, UserResponseWrapper.class))
                .thenReturn(null);
        assertThrows(UserServiceUnavailableException.class, () -> userService.getUser(userName));
    }

    @Test
    void testGetUserNoResponseData() {
        UserResponseWrapper userResponseWrapper = new UserResponseWrapper();
        when(restTemplate.getForObject(userServiceEndpoint + userName, UserResponseWrapper.class))
                .thenReturn(userResponseWrapper);
        assertNull(userService.getUser(userName));
    }

    @Test
    void testGetUserErrorMessage() {
        UserResponseWrapper userResponseWrapper = new UserResponseWrapper();
        userResponseWrapper.setErrorMessage("User not found");
        when(restTemplate.getForObject(userServiceEndpoint + userName, UserResponseWrapper.class))
                .thenReturn(userResponseWrapper);
        assertNull(userService.getUser(userName));
    }

    @Test
    void testGetUserUpstreamError() {
        UserResponseWrapper userResponseWrapper = new UserResponseWrapper();
        userResponseWrapper.setErrorMessage("User not found");
        assertThrows(UserServiceUnavailableException.class, () -> userService.getUser(userName));
    }

    @Test
    void testGetUserUpstreamExpectedError() {
        UserResponseWrapper userResponseWrapper = new UserResponseWrapper();
        userResponseWrapper.setErrorMessage("User not found");
        when(restTemplate.getForObject(userServiceEndpoint + userName, UserResponseWrapper.class))
                .thenThrow(new RestClientException("Could not find user"));
        assertNull(userService.getUser(userName));
    }

    @Test
    void testUserExists() {
        UserResponse userResponse = new UserResponse(userName, "test2", UserRole.STUDENT);
        UserResponseWrapper userResponseWrapper = new UserResponseWrapper();
        userResponseWrapper.setData(userResponse);
        when(restTemplate.getForObject(userServiceEndpoint + userName, UserResponseWrapper.class))
                .thenReturn(userResponseWrapper);
        assertTrue(userService.userExists(userName));
    }
}
