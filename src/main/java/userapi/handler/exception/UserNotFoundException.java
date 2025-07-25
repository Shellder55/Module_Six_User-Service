package userapi.handler.exception;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(Long id) {
        super("User not found by ID: " + id);
    }
}
