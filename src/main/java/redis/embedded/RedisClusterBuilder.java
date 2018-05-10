package redis.embedded;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class RedisClusterBuilder {

    private List<ClusterMaster> masters = new LinkedList<>();

    private List<ClusterSlave> slaves = new LinkedList<>();

    private String meetRedisIp = null;
    private Integer meetRedisPort = null;

    private RedisServerBuilder serverBuilder = new RedisServerBuilder();

    private Integer clusterNodeTimeoutMS = 30000; //Default timeout is 3 seconds.
    private String basicAuthPassword = null;

    public RedisClusterBuilder masters(Collection<ClusterMaster> masters) {
        this.masters.addAll(masters);
        return this;
    }

    public RedisClusterBuilder slaves(Collection<ClusterSlave> slaves) {
        this.slaves.addAll(slaves);
        return this;
    }

    public RedisClusterBuilder meetWith(String ipAddress, Integer port) {
        this.meetRedisIp = ipAddress;
        this.meetRedisPort = port;
        return this;
    }

    public RedisClusterBuilder clusterNodeTimeoutMS(Integer clusterNodeTimeoutMS) {
        this.clusterNodeTimeoutMS = clusterNodeTimeoutMS;
        return this;
    }

    public RedisClusterBuilder basicAuthPassword(String password) {
        this.basicAuthPassword = password;
        return this;
    }

    public RedisCluster build() {
        buildMasters();
        buildSlaves();
        return new RedisCluster(masters, slaves, meetRedisIp, meetRedisPort, basicAuthPassword);
    }

    public void buildMasters() {
        for (ClusterMaster master : masters) {
            serverBuilder.reset();
            serverBuilder.port(master.getMasterRedisPort());
            serverBuilder.setting("cluster-enabled yes");
            serverBuilder.setting("cluster-node-timeout " + clusterNodeTimeoutMS);
            if (!master.getMasterRedisIp().equals("127.0.0.1")) {
                serverBuilder.setting("bind " + master.getMasterRedisIp() + " 127.0.0.1");
            }
            if(basicAuthPassword != null) {
                serverBuilder.setting("requirepass " + basicAuthPassword);
            }
            final RedisServer redisMaster = serverBuilder.build();
            master.setMasterRedis(redisMaster);
        }
    }

    public void buildSlaves() {
        for (ClusterSlave slave : this.slaves) {
            serverBuilder.reset();
            serverBuilder.port(slave.getSlavePort());
            serverBuilder.setting("cluster-enabled yes");
            serverBuilder.setting("cluster-node-timeout " + clusterNodeTimeoutMS);
            if (!slave.getMasterRedisIp().equals("127.0.0.1")) {
                serverBuilder.setting("bind " + slave.getSlaveIp() + " 127.0.0.1");
            }
            if(basicAuthPassword != null) {
                serverBuilder.setting("requirepass " + basicAuthPassword);
            }
            final RedisServer redisSlave = serverBuilder.build();
            slave.setSlaveRedis(redisSlave);
        }
    }
}
