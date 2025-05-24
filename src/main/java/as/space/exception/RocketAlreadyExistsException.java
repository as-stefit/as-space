package as.space.exception;

public class RocketAlreadyExistsException extends RuntimeException{
    public RocketAlreadyExistsException(String name) {
        super("Rocket with name '" + name + "' already exists.");
    }
}
