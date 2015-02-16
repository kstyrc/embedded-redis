package redis.embedded;

import com.google.common.collect.Lists;
import redis.embedded.exceptions.EmbeddedRedisException;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

    @Override
    public List<Integer> ports() {
        return Stream.concat(
                sentinels.stream().flatMap(s -> s.ports().stream()),
                servers.stream().flatMap(s -> s.ports().stream())
        ).collect(Collectors.toList());
    }

    public List<Redis> sentinels() {
        return Lists.newLinkedList(sentinels);
    }

    public List<Integer> sentinelPorts() {
        return sentinels.stream().flatMap(s -> s.ports().stream()).collect(Collectors.toList());
    }

    public List<Redis> servers() {
        return Lists.newLinkedList(servers);
    }

    public List<Integer> serverPorts() {
        return servers.stream().flatMap(s -> s.ports().stream()).collect(Collectors.toList());
    }

    public static RedisClusterBuilder builder() {
        return new RedisClusterBuilder();
    }
}
