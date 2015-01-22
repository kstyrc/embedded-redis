package redis.embedded;

import java.io.IOException;

/**
 * Created by piotrturek on 22/01/15.
 */
public interface RedisInstance {
    boolean isActive();

    void start() throws IOException;

    void stop() throws InterruptedException;
}
