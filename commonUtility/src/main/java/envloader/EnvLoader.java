package envloader;

public interface EnvLoader {
    /**
     * Gets the environment variable with the given key.
     *
     * @param key The key
     * @return The value
     */
    String get(String key);

    /**
     * Gets the environment variable with the given key. If it does not exist, the defaultValue is
     * returned.
     *
     * @param key          The key
     * @param defaultValue the default value
     * @return The value
     */
    String get(String key, String defaultValue);
}
