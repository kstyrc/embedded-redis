package redis.embedded.exceptions;

/**
 * Created by piotrturek on 22/01/15.
 */
public class EmbeddedRedisException extends RuntimeException {
    public EmbeddedRedisException(String message, Throwable cause) {
        super(message, cause);
    }

    public EmbeddedRedisException(String message) {
        super(message);
    }
}
