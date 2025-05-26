package as.space.exception;

public class OperationNotAllowedException extends RuntimeException {
    public OperationNotAllowedException(String message) {
        super("This operation is not allowed. " + message);
    }
}
