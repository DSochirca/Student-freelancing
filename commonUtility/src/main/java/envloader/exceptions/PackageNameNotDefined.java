package envloader.exceptions;

public class PackageNameNotDefined extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public PackageNameNotDefined() {
    }

    public PackageNameNotDefined(String message) {
        super(message);
    }
}
