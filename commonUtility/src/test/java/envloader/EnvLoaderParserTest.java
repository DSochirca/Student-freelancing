package envloader;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.FileNotFoundException;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class EnvLoaderParserTest {
    private transient final String path = "src/main/resources/someFolder";
    private transient final String file = "fake.env";
    private transient EnvLoaderReader envLoaderReader;
    private transient EnvLoaderParser parser;

    @BeforeEach
    void setUp() {
        envLoaderReader = mock(EnvLoaderReader.class);
        parser = new EnvLoaderParser(path, file, envLoaderReader);
    }

    @Test
    void parseNull() throws FileNotFoundException {
        when(envLoaderReader.read(path, file)).thenReturn(null);

        assertNull(parser.parse());
    }

    @Test
    void parseEmpty() throws FileNotFoundException {
        when(envLoaderReader.read(path, file)).thenReturn(List.of(""));

        assertTrue(parser.parse().isEmpty());

        when(envLoaderReader.read(path, file)).thenReturn(List.of(" "));

        assertTrue(parser.parse().isEmpty());
    }

    @Test
    void parseInvalid() throws FileNotFoundException {
        when(envLoaderReader.read(path, file)).thenReturn(List.of("hello world"));

        assertTrue(parser.parse().isEmpty());
    }

    @Test
    void parseComment() throws FileNotFoundException {
        when(envLoaderReader.read(path, file)).thenReturn(List.of("#hello=world"));

        assertTrue(parser.parse().isEmpty());
    }
}