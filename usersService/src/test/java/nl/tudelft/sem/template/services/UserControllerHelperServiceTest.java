package nl.tudelft.sem.template.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import nl.tudelft.sem.template.domain.dtos.Response;
import nl.tudelft.sem.template.domain.dtos.UserLoginResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;



@SpringBootTest
@AutoConfigureMockMvc
public class UserControllerHelperServiceTest {

    @Test
    void testCreateUserResponse() {
        assertEquals(
                UserControllerHelperService.createUserResponse(null, HttpStatus.CREATED),
                new ResponseEntity<>(new Response<>(null, null), HttpStatus.CREATED)
        );
    }

    @Test
    void testCreateErrorResponse() {
        String err = "error";
        assertEquals(
                UserControllerHelperService.createErrorResponse(err, HttpStatus.FORBIDDEN),
                new ResponseEntity<>(new Response<>(null, err), HttpStatus.FORBIDDEN)
        );
    }

    @Test
    void testCreateLoginResponse() {
        String token = "token";
        ResponseEntity<Response<UserLoginResponse>> response
                = UserControllerHelperService.createLoginResponse(token);
        assertNotNull(response.getBody());
        assertEquals(response.getBody().getData().getToken(), token);
        assertEquals(response.getStatusCode(), HttpStatus.OK);
    }

    @Test
    void testCreateInvalidLoginResponse() {
        ResponseEntity<Response<UserLoginResponse>> response
                = UserControllerHelperService.createInvalidLoginResponse();
        assertNotNull(response.getBody());
        assertNull(response.getBody().getData().getToken());
        assertEquals(response.getStatusCode(), HttpStatus.UNAUTHORIZED);
    }

    @Test
    void testIsAdmin() {
        assertEquals(true, UserControllerHelperService.isAdmin("ADMIN"));
    }

    @Test
    void testIsNotAdmin() {
        assertEquals(false, UserControllerHelperService.isAdmin("USER"));
    }

}
