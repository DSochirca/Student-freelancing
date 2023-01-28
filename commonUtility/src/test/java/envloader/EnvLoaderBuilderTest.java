package envloader;

import static org.junit.jupiter.api.Assertions.*;

import envloader.exceptions.PackageNameNotDefined;
import java.io.FileNotFoundException;
import java.util.HashMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class EnvLoaderBuilderTest {
    private transient EnvLoaderBuilder envLoaderBuilder;

    @BeforeEach
    void setUp() {
        envLoaderBuilder = new EnvLoaderBuilder();
    }

    @Test
    void loadExceptions() {
        assertThrows(PackageNameNotDefined.class, () -> envLoaderBuilder.load());
        assertThrows(FileNotFoundException.class, () -> envLoaderBuilder.packageName("abc").load());
    }

    @Test
    void loadTest() {
        try {
//            EnvLoader env = envLoaderBuilder
//                .packageName("envLoader")
//                .path("src/main/resources/someFolder")
//                .filename("fake.env")
//                .load();
//
//            assertEquals("world", env.get("hello"));
//            assertEquals("!", env.get("hello, world", "!"));
//            assertEquals("world", env.get("hello", "!"));

            assertEquals(new EnvLoaderImpl(new HashMap<>()), envLoaderBuilder
                .packageName("envLoader")
                .filename("no")
                .doNotThrowFileNotFoundException()
                .load());
        } catch (FileNotFoundException e) {
            fail(e);
        }
    }
}