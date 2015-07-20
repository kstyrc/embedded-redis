package redis.embedded;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by dragan on 17.07.15.
 */
public class RealRedisClusterBuilder {
    Collection<Integer> ports;
    private int numOfReplicates = 1;

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

    public RealRedisCluster build() {
        final List<Redis> servers = buildServers();
        return new RealRedisCluster(servers, numOfReplicates);
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