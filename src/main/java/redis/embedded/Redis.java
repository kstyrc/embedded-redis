package redis.embedded;

import redis.embedded.exceptions.EmbeddedRedisException;

import java.util.List;

public interface Redis {
    boolean isActive();

    void start() throws EmbeddedRedisException;

    void start(String redisReadyPattern) throws EmbeddedRedisException;

    void stop() throws EmbeddedRedisException;

    List<Integer> ports();
}
