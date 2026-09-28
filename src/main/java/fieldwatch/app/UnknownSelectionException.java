package fieldwatch.app;

/** Thrown when the input names a mission type or controller scheme nobody registered. */
public class UnknownSelectionException extends RuntimeException {

    public UnknownSelectionException(String message) {
        super(message);
    }
}
