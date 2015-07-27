package redis.embedded.cluster;

import redis.embedded.Redis;
import redis.embedded.RedisServer;
import redis.embedded.RedisServerBuilder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by dragan on 17.07.15.
 */
public class RealRedisClusterBuilder {
    private static final int DEFAULT_REPLICATES = 1;
    private static final int DEFAULT_NUMBER_RETRIES = 5;
    Collection<Integer> ports;
    private int numOfReplicates;
    private int numOfRetries;

    private RedisServerBuilder serverBuilder = new RedisServerBuilder();


    public RealRedisClusterBuilder withServerBuilder(RedisServerBuilder serverBuilder) {
        this.serverBuilder = serverBuilder;
        return this;
    }

    public RealRedisClusterBuilder serverPorts(Collection<Integer> ports) {
        this.ports = ports;
        return this;
    }

    public RealRedisClusterBuilder numOfReplicates(int numOfReplicates) {
        this.numOfReplicates = numOfReplicates;
        return this;
    }

    public RealRedisClusterBuilder numOfRetries(int numOfRetries) {
        this.numOfRetries = numOfRetries;
        return this;
    }

    public RealRedisCluster build() {
        final List<Redis> servers = buildServers();
        if (numOfReplicates == 0) {
            numOfReplicates = DEFAULT_REPLICATES;
        }
        if (numOfRetries == 0) {
            numOfRetries = DEFAULT_NUMBER_RETRIES;
        }
        return new RealRedisCluster(servers, numOfReplicates, numOfRetries);
    }

    private List<Redis> buildServers() {
        List<Redis> servers = new ArrayList<Redis>();
        for (Integer port : ports) {
            servers.add(buildNode(port));
        }
        return servers;
    }

    private RedisServer buildNode(Integer port) {
        serverBuilder.reset();
        serverBuilder.setting("cluster-enabled yes");
        serverBuilder.setting("cluster-config-file nodes-" + port + ".conf");
        serverBuilder.setting("cluster-node-timeout 2000");
        serverBuilder.setting("appendonly yes");
        serverBuilder.setting("dbfilename dump-" + port + ".rdb");
        return serverBuilder.port(port).build();
    }

}