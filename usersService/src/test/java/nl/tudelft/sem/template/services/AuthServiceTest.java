package nl.tudelft.sem.template.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Date;
import nl.tudelft.sem.template.entities.JwtConfig;
import nl.tudelft.sem.template.entities.StudentFactory;
import nl.tudelft.sem.template.entities.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;


@SpringBootTest
@AutoConfigureMockMvc
public class AuthServiceTest {

    @Autowired
    private transient AuthService authService;

    @MockBean
    private transient BCryptPasswordEncoder passwordEncoder;

    @MockBean
    private transient JwtConfig jwtConfig;

    private transient User user;

    @BeforeEach
    void setUp() {
        user = new StudentFactory().createUser("testing", "testPass");
    }

    @Test
    void testHashPassword() {
        String password = "test";
        String hashed = "hashedPassword";
        when(passwordEncoder.encode(password)).thenReturn(hashed);

        String hashedPassword = authService.hashPassword(password);
        assertEquals(hashed, hashedPassword);
    }

    @Test
    void testVerifyPasswordCorrect() {
        String password = "test_password";
        User hashedPasswordUser = new StudentFactory()
                .createUser("test2", "hashedPassword");

        when(passwordEncoder.matches(password, hashedPasswordUser.getPassword()))
                .thenReturn(true);

        assertTrue(authService.verifyPassword(hashedPasswordUser, password));
        verify(passwordEncoder).matches(password, hashedPasswordUser.getPassword());
    }

    @Test
    void testVerifyPasswordWrongPassword() {
        String password = "test_password";
        User hashedPasswordUser = new StudentFactory()
                .createUser("test2", "abc");

        when(passwordEncoder.matches(password, hashedPasswordUser.getPassword()))
                .thenReturn(false);

        assertFalse(authService.verifyPassword(hashedPasswordUser, password));
    }

    @Test
    void testGenerateJwtToken() {
        when(jwtConfig.getJwtSecret()).thenReturn("secret");
        when(jwtConfig.getLifetime()).thenReturn((long) (60 * 60 * 1000));

        String token = authService.generateJwtToken(user);
        assertNotNull(token);
        DecodedJWT decodedToken = JWT.decode(token);

        assertEquals(user.getUsername(), decodedToken.getClaim("userName").asString());
        assertEquals(user.getRole().toString(), decodedToken.getClaim("userRole").asString());
    }

    @Test
    void testGenerateJwtTokenTestDate() {
        when(jwtConfig.getJwtSecret()).thenReturn("secret");
        when(jwtConfig.getLifetime()).thenReturn((long) (60 * 60 * 1000));

        //Mock the instant time clock:
        String instantExpected = "2025-12-22T10:15:30Z";
        Clock clock = Clock.fixed(Instant.parse(instantExpected), ZoneId.of("UTC"));
        Instant instant = Instant.now(clock);

        try (MockedStatic<Instant> mockedStatic = mockStatic(Instant.class)) {
            mockedStatic.when(Instant::now).thenReturn(instant);
            Instant now = Instant.now();
            assertEquals(now.toString(), instantExpected);
        }
        //----------------------------

        String token = authService.generateJwtToken(user);
        assertNotNull(token);
        DecodedJWT decodedToken = JWT.decode(token);

        Date date = decodedToken.getExpiresAt();
        Date dateExpected = new Date(Instant.now().toEpochMilli() + (60 * 60 * 1000));
        assertEquals(dateExpected.toString(), date.toString());
    }

    @Test
    void testDefaultAdminPassword() {
        when(authService.getAdminPassword()).thenReturn("password");

        String password = authService.getAdminPassword();

        assertEquals("password", password);
        verify(jwtConfig, atLeast(1)).getAdminPassword();
    }

}
