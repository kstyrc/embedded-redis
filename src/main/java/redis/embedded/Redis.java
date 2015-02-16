package redis.embedded;

import redis.embedded.exceptions.EmbeddedRedisException;

import java.util.List;

/**
 * Created by piotrturek on 22/01/15.
 */
public interface Redis {
    boolean isActive();

    void start() throws EmbeddedRedisException;

    void stop() throws EmbeddedRedisException;

    List<Integer> ports();
}
