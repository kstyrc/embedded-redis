package redis.embedded.exceptions;

public class RedisBuildingException extends RuntimeException {
    public RedisBuildingException(String message, Throwable cause) {
        super(message, cause);
    }

    public RedisBuildingException(String message) {
        super(message);
    }
}
