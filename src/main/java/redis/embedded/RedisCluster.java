package redis.embedded;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCluster;
import redis.embedded.exceptions.EmbeddedRedisException;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RedisCluster implements Redis {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final List<ClusterMaster> masters = new LinkedList();
    private final List<ClusterSlave> slaves = new LinkedList();

    private Integer DEFAULT_TIMEOUT_WAIT_CLUSTER = 0; // 0 means, no timeout
    private Integer waitForClusterTimeoutMS = DEFAULT_TIMEOUT_WAIT_CLUSTER;

    private String meetRedisIp = null;
    private Integer meetRedisPort = null;
    private String basicAuthPassword = null;

    RedisCluster(List<ClusterMaster> masters, List<ClusterSlave> slaves,
                 String meetRedisIp, Integer meetRedisPort,
                 String basicAuthPassword) {
        this.masters.addAll(masters);
        this.slaves.addAll(slaves);
        this.meetRedisIp = meetRedisIp;
        this.meetRedisPort = meetRedisPort;
        this.basicAuthPassword = basicAuthPassword;
    }

    @Override
    public boolean isActive() {
        for(ClusterMaster master : masters) {
            if(!master.getMasterRedis().isActive()) {
                return false;
            }
        }
        for(ClusterSlave slave : slaves) {
            if(!slave.getSlaveRedis().isActive()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void start() throws EmbeddedRedisException {
        for (ClusterMaster master : masters) {
            master.getMasterRedis().start();

            if (meetRedisIp != null && meetRedisPort != null) {
                Jedis jedis = new Jedis(master.getMasterRedisIp(), master.getMasterRedisPort());
                if(this.basicAuthPassword != null) {
                    jedis.auth(this.basicAuthPassword);
                }
                jedis.clusterMeet(meetRedisIp, meetRedisPort);
            }
        }
        allocSlotsToMaster();

        for (ClusterSlave slave : slaves) {
            slave.getSlaveRedis().start();

            final Jedis jedisSlave = new Jedis(slave.getSlaveIp(), slave.getSlavePort());

            if(this.basicAuthPassword != null) {
                jedisSlave.auth(this.basicAuthPassword);
            }
            if (meetRedisIp != null && meetRedisPort != null) {
                jedisSlave.clusterMeet(meetRedisIp, meetRedisPort);
            }

            final Jedis jedisMaster = new Jedis(slave.getMasterRedisIp(), slave.getMasterRedisPort());

            if(this.basicAuthPassword != null) {
                jedisMaster.auth(this.basicAuthPassword);
            }


            ExecutorService executor = Executors.newSingleThreadExecutor();
            executor.submit(() -> setReplica(jedisMaster, jedisSlave));

        }
    }

    @Override
    public void stop() throws EmbeddedRedisException {
        for(ClusterMaster master : masters) {
            master.getMasterRedis().stop();
        }
        for(ClusterSlave slave : slaves) {
            slave.getSlaveRedis().stop();
        }
    }

    @Override
    public List<Integer> ports() {
        List<Integer> ports = new ArrayList<Integer>();
        ports.addAll(masterPorts());
        ports.addAll(slavesPorts());
        return ports;
    }

    public List<Integer> masterPorts() {
        List<Integer> ports = new ArrayList<Integer>();
        for(ClusterMaster master : masters) {
            ports.addAll(master.getMasterRedis().ports());
        }
        return ports;
    }

    public List<Integer> slavesPorts() {
        List<Integer> ports = new ArrayList<Integer>();
        for(ClusterSlave slave : slaves) {
            ports.addAll(slave.getSlaveRedis().ports());
        }
        return ports;
    }

    public static RedisClusterBuilder builder() {
        return new RedisClusterBuilder();
    }

    private void allocSlotsToMaster() {

        for(ClusterMaster master : masters) {

            int slotsPerNode = JedisCluster.HASHSLOTS / master.getClusterNodesCount();
            int remainedSlot = JedisCluster.HASHSLOTS % master.getClusterNodesCount();
            int additionalSlot = 0;

            int[] nodeSlots;
            if(master.getClusterNodesCount().equals(master.getIndexOfCluster() + 1)) {
                nodeSlots = new int[slotsPerNode + remainedSlot];
                additionalSlot = remainedSlot;
            } else {
                nodeSlots = new int[slotsPerNode];
            }

            Integer startSlot = master.getIndexOfCluster() * slotsPerNode;
            Integer endSlot = (master.getIndexOfCluster() + 1) * slotsPerNode -1 + additionalSlot;
            for (int i = startSlot, slot = 0 ; i <= endSlot; i++) {
                nodeSlots[slot++] = i;
            }

            Jedis jedis = new Jedis(master.getMasterRedisIp(), master.getMasterRedisPort());

            if(this.basicAuthPassword != null) {
                jedis.auth(this.basicAuthPassword);
            }

            jedis.clusterAddSlots(nodeSlots);
        }
    }

    public void setReplica(Jedis master, Jedis slave) {

        try {
            waitForClusterReady(master);
        } catch (EmbeddedRedisException e) {
            logger.error(e.getMessage());
        }

        for (String nodeInfo : master.clusterNodes().split("\n")) {
            logger.debug("[setReplica]" + nodeInfo);
            if (nodeInfo.contains("myself")) {
                slave.clusterReplicate(nodeInfo.split(" ")[0]);
                break;
            }
        }
    }

    public void waitForClusterReady(Jedis... nodes) throws EmbeddedRedisException {
        boolean clusterOk = false;

        long beginTime = System.currentTimeMillis();

        while (!clusterOk) {
            boolean isOk = true;
            for (Jedis node : nodes) {
                logger.debug("[waitForClusterReady]" + node.clusterInfo());
                if (!node.clusterInfo().split("\n")[0].contains("ok")) {
                    isOk = false;
                    break;
                }
            }

            if (isOk) {
                clusterOk = true;
            }

            long currentTime = System.currentTimeMillis();
            long elapsedTimeMS = currentTime - beginTime;

            if(getWaitForClusterTimeoutMS() != 0) {
                if (elapsedTimeMS > getWaitForClusterTimeoutMS()) {
                    throw new EmbeddedRedisException("Time out for waiting ready of cluster");
                }
            }

            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                throw new EmbeddedRedisException("InterruptedException is raised", e);
            }
        }
    }

    public Integer getWaitForClusterTimeoutMS() {
        return waitForClusterTimeoutMS;
    }

    public RedisCluster setWaitForClusterTimeoutMS(Integer waitForClusterTimeoutMS) {
        this.waitForClusterTimeoutMS = waitForClusterTimeoutMS;
        return this;
    }
}
