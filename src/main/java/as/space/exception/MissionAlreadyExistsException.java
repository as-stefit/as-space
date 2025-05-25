package as.space.exception;

public class MissionAlreadyExistsException extends RuntimeException {
  public MissionAlreadyExistsException(String name) {
    super("Mission with name '" + name + "' already exists.");
  }
}
