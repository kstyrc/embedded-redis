package redis.embedded;

import java.util.ArrayList;
import java.util.List;

public class RedisSentinel extends AbstractRedisInstance {
    static final String REDIS_SENTINEL_READY_PATTERN = ".*Sentinel runid is.*";

    public RedisSentinel(List<String> args, int port) {
        this(args, port, REDIS_SENTINEL_READY_PATTERN);
    }

    public RedisSentinel(List<String> args, int port, String readyPattern) {
        super(port, readyPattern);
        this.args = new ArrayList<String>(args);
    }

    public static RedisSentinelBuilder builder() { return new RedisSentinelBuilder(); }

}
