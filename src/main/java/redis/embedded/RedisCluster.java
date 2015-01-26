package redis.embedded;

import redis.embedded.exceptions.EmbeddedRedisException;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by piotrturek on 22/01/15.
 */
public class RedisCluster implements Redis {
    private final List<Redis> sentinels = new LinkedList<>();
    private final List<Redis> servers = new LinkedList<>();

    RedisCluster(List<Redis> sentinels, List<Redis> servers) {
        this.servers.addAll(servers);
        this.sentinels.addAll(sentinels);
    }

    @Override
    public boolean isActive() {
        return sentinels.stream().allMatch(Redis::isActive) && servers.stream().allMatch(Redis::isActive);
    }

    @Override
    public void start() throws EmbeddedRedisException {
        sentinels.parallelStream().forEach(Redis::start);
        servers.parallelStream().forEach(Redis::start);
    }

    @Override
    public void stop() throws EmbeddedRedisException {
        servers.parallelStream().forEach(Redis::stop);
        sentinels.parallelStream().forEach(Redis::stop);
    }

    public static RedisClusterBuilder builder() {
        return new RedisClusterBuilder();
    }
}
