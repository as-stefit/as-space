package as.space.exception;

public class MissionNotFoundException extends RuntimeException {
  public MissionNotFoundException(String name) {
    super("Mission with name '" + name + "' does not exist.");
  }
}
