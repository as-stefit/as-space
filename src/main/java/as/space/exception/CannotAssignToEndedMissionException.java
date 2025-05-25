package as.space.exception;

public class CannotAssignToEndedMissionException extends RuntimeException {
    public CannotAssignToEndedMissionException(String name) {
        super("Mission with name '" + name + "' already ended. Cannot assign to ended mission.");
    }
}
