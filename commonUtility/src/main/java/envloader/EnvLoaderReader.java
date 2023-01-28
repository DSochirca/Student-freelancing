package envloader;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;

class EnvLoaderReader {
    /**
     * Reads the contents of the given file and returns it as a list of lines.
     *
     * @param directory The directory the file is located at relative to the root.
     * @param filename The filename.
     * @return A list of the lines.
     * @throws FileNotFoundException If the file is not found
     */
    public List<String> read(String directory, String filename) throws FileNotFoundException {
        var location = (directory + "/" + filename).toUpperCase(Locale.ROOT);
        var path = Path.of(new File(location).getAbsolutePath());

        if (Files.exists(path)) {
            try {
                return Files.readAllLines(path);
            } catch (IOException e) {
                return null;
            }
        } else {
            throw new FileNotFoundException("Could not find file: " + path);
        }
    }
}
