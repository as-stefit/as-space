package as.space.exception;

public class RocketAlreadyAssignedException extends RuntimeException {
    public RocketAlreadyAssignedException(String name) {
        super("Rocket with name '" + name + "' already assigned to mission.");
    }
}
