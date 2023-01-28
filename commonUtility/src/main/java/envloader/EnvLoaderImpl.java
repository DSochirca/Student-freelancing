package envloader;

import java.util.Map;
import java.util.Objects;

class EnvLoaderImpl implements EnvLoader {
    private final transient Map<String, String> envVars;

    public EnvLoaderImpl(Map<String, String> envVars) {
        this.envVars = envVars;
    }

    @Override
    public String get(String key) {
        return this.envVars.get(key);
    }

    @Override
    public String get(String key, String defaultValue) {
        var value = this.envVars.get(key);

        return value != null ? value : defaultValue;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof EnvLoaderImpl)) {
            return false;
        }

        EnvLoaderImpl envLoader = (EnvLoaderImpl) o;
        return envVars.equals(envLoader.envVars);
    }

    @Override
    public int hashCode() {
        return Objects.hash(envVars);
    }
}
