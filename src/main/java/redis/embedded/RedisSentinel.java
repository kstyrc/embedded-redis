package redis.embedded;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by piotrturek on 22/01/15.
 */
public class RedisSentinel extends AbstractRedisInstance {
    private static final String REDIS_READY_PATTERN = ".*Sentinel runid is.*";

    public RedisSentinel(List<String> args, int port) {
        super(port);
        this.args = new ArrayList<>(args);
    }

    public static RedisSentinelBuilder builder() { return new RedisSentinelBuilder(); }

    @Override
    protected String redisReadyPattern() {
        return REDIS_READY_PATTERN;
    }
}
