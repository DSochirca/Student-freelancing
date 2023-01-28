package envloader;

import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

class EnvLoaderParser {
    private final transient String directory;
    private final transient String filename;
    private final transient Function<String, Boolean> isComment = s -> s.startsWith("#");
    private final transient Function<String, Boolean> isBlank = s -> s.trim().isEmpty();
    private final transient Function<String, Boolean> isInvalid = s -> !s.contains("=");
    private final transient Function<String, Boolean> shouldNotSkip =
        s -> !isComment.apply(s) && !isBlank.apply(s) && !isInvalid.apply(s);

    private transient EnvLoaderReader envLoaderReader = new EnvLoaderReader();

    public EnvLoaderParser(String path, String directory) {
        this.directory = path;
        this.filename = directory;
    }

    EnvLoaderParser(String path, String directory, EnvLoaderReader envLoaderReader) {
        this.directory = path;
        this.filename = directory;
        this.envLoaderReader = envLoaderReader;
    }

    /**
     * Parses the file.
     *
     * @return A map of key value pairs for the environment variables.
     * @throws FileNotFoundException If the file does not exist.
     */
    public Map<String, String> parse() throws FileNotFoundException {
        var lines = envLoaderReader.read(directory, filename);

        if (lines == null) {
            return null;
        }

        Map<String, String> res = new HashMap<>();
        lines.stream().filter(shouldNotSkip::apply).forEach(line -> {
            var split = line.split("=");
            res.put(split[0], split[1]);
        });

        return res;
    }
}
