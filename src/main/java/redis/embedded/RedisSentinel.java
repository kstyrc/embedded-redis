package redis.embedded;

import java.util.ArrayList;
import java.util.List;

public class RedisSentinel extends AbstractRedisInstance {
    /**
     * Redis < 3.2 outputs "Sentinel runid is"
     * Redis 3.2+ outputs "Sentinel ID is"
     */
    private static final String REDIS_READY_PATTERN = ".*(Sentinel runid is|Sentinel ID is).*";

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
