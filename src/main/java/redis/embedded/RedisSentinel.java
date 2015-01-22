package redis.embedded;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by piotrturek on 22/01/15.
 */
public class RedisSentinel extends AbstractRedisInstance {
    public RedisSentinel(List<String> args) {
        this.args = new ArrayList<>(args);
    }

    public static RedisSentinelBuilder builder() { return new RedisSentinelBuilder(); }
}
