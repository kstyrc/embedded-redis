package redis.embedded.util;

import redis.embedded.Redis;
import redis.embedded.RedisCluster;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by piotrturek on 01/02/15.
 */
public class JedisUtil {
    public static Set<String> jedisHosts(Redis redis) {
        final List<Integer> ports = redis.ports();
        return portsToJedisHosts(ports);
    }

    public static Set<String> sentinelHosts(RedisCluster cluster) {
        final List<Integer> ports = cluster.sentinelPorts();
        return portsToJedisHosts(ports);
    }

    public static Set<String> portsToJedisHosts(List<Integer> ports) {
        return ports.stream().map(p -> "localhost:" + p).collect(Collectors.toSet());
    }
}
