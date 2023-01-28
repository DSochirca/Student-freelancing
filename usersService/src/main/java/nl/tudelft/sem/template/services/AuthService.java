package nl.tudelft.sem.template.services;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.sun.istack.NotNull;
import java.time.Instant;
import java.util.Date;
import nl.tudelft.sem.template.entities.JwtConfig;
import nl.tudelft.sem.template.entities.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;


@Service
public class AuthService {

    @Autowired
    private transient BCryptPasswordEncoder bcryptPasswordEncoder;

    @Autowired
    private transient JwtConfig jwtConfig;

    /**
     * Hash a password.
     *
     * @param password plaintext password to hash.
     * @return hashed password
     */
    public String hashPassword(@NotNull String password) {
        return bcryptPasswordEncoder.encode(password);
    }

    /**
     * Verify if the password matches the user's password.
     *
     * @param user user to verify password for.
     * @param password plaintext password to verify.
     * @return true if password matches, false otherwise.
     */
    public Boolean verifyPassword(User user, String password) {
        return bcryptPasswordEncoder.matches(password, user.getPassword());
    }

    /** Generate a JWT token for the user.
     *
     * @param user user to generate token for.
     * @return generated token
     */
    public String generateJwtToken(User user) {
        Algorithm algorithm = Algorithm.HMAC256(jwtConfig.getJwtSecret());
        return JWT.create()
                .withExpiresAt(new Date(Instant.now().toEpochMilli() + jwtConfig.getLifetime()))
                .withIssuer("SEM3B-TUD")
                .withClaim("userName", user.getUsername())
                .withClaim("userRole", user.getRole().toString())
                .sign(algorithm);
    }

    public String getAdminPassword() {
        return jwtConfig.getAdminPassword();
    }
}
