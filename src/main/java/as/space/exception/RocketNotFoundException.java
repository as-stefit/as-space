package as.space.exception;

public class RocketNotFoundException extends RuntimeException {
  public RocketNotFoundException(String name) {
    super("Rocket with name '" + name + "' does not exist.");
  }
}
