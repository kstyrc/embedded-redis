package redis.embedded.exceptions;

/**
 * Created by piotrturek on 26/01/15.
 */
public class RedisBuildingException extends RuntimeException {
    public RedisBuildingException(String message, Throwable cause) {
        super(message, cause);
    }
}
