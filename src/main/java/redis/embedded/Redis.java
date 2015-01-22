package redis.embedded;

import redis.embedded.exceptions.EmbeddedRedisException;

/**
 * Created by piotrturek on 22/01/15.
 */
public interface Redis {
    boolean isActive();

    void start() throws EmbeddedRedisException;

    void stop() throws EmbeddedRedisException;
}
