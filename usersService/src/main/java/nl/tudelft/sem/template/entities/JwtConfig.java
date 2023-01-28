package nl.tudelft.sem.template.entities;

import envloader.EnvLoaderBuilder;
import java.io.FileNotFoundException;
import lombok.Data;
import org.springframework.stereotype.Component;

@Data
@Component
public class JwtConfig {
    private final String jwtSecret;
    private final String adminPassword;
    private final long lifetime;

    /**
     * Creates a new JwtConfig instance.
     *
     * @throws FileNotFoundException If the env file is not found and the
     * <code>throwFileNotFoundException()</code> method is used.
     */
    public JwtConfig() throws FileNotFoundException {
        var loader = new EnvLoaderBuilder()
            .packageName("usersService")
            .doNotThrowFileNotFoundException()
            .load();

        jwtSecret = loader.get("JWT_SECRET", "secret");
        adminPassword = loader.get("ADMIN_PASSWORD", "admin");
        lifetime = Long.parseLong(loader.get("JWT_LIFETIME",  "" + (60 * 60 * 1000)));
    }
}
