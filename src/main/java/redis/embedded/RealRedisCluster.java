package redis.embedded;

import redis.clients.jedis.Jedis;
import redis.embedded.exceptions.EmbeddedRedisException;

import java.util.*;

/**
 * Created by dragan on 17.07.15.
 */
public class RealRedisCluster implements Cluster {
    private static final int CLUSTER_HASH_SLOTS_NUMBER = 16384;
    private static final String LOCAL_HOST = "127.0.0.1";

    private final List<Redis> servers = new LinkedList<Redis>();
    private final int numOfReplicates;

    RealRedisCluster(List<Redis> servers, int numOfReplicates) {
        validateParams(servers, numOfReplicates);
        this.servers.addAll(servers);
        this.numOfReplicates = numOfReplicates;
    }

    RealRedisCluster(List<Redis> servers) {
        validateParams(servers, 1);
        this.servers.addAll(servers);
        this.numOfReplicates = 1;
    }

    private void validateParams(List<Redis> servers, int numOfReplicates) {
        if (servers.size() <= 2) {
            throw new EmbeddedRedisException("Redis Cluster requires at least 3 master nodes.");
        }
        if (numOfReplicates < 1) {
            throw new EmbeddedRedisException("Redis Cluster requires at least 1 replication.");
        }
        if (numOfReplicates > servers.size() - 1) {
            throw new EmbeddedRedisException("Redis Cluster requires number of replications less than number of nodes - 1.");
        }
    }

    @Override
    public void create() throws EmbeddedRedisException {
        for (Redis redis : servers) {
            redis.start();
        }
    }

    @Override
    public void start() throws EmbeddedRedisException {
        List<MasterNode> masters = allocSlots();
        joinCluster();
        System.out.println("Waiting for the cluster to join...");
        while (!clusterState().equals(ClusterState.OK)) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new EmbeddedRedisException(e.getMessage(), e);
            }
        }
        setReplicates(masters);
    }

    @Override
    public boolean isActive() {
        for (Redis redis : servers) {
            if (!redis.isActive()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void stop() throws EmbeddedRedisException {
        for (Redis redis : servers) {
            redis.stop();
        }
    }

    @Override
    public List<Integer> ports() {
        List<Integer> ports = new ArrayList<Integer>();
        for (Redis redis : servers) {
            ports.addAll(redis.ports());
        }
        return ports;
    }

    private ClusterState clusterState() {
        Redis redis = servers.get(0);
        Jedis jedis = null;
        try {
            jedis = new Jedis(LOCAL_HOST, redis.ports().get(0));
            String ack = jedis.clusterInfo();
            return ClusterState.getStateByStr(ack.split("\r\n")[0].split(":")[1]);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    private void joinCluster() {
        Jedis jedis = null;

        //connect sequentially nodes
        for (int i = 0; i < servers.size() - 1; i++) {
            try {
                jedis = new Jedis(LOCAL_HOST, servers.get(i).ports().get(0));
                jedis.clusterMeet(LOCAL_HOST, servers.get(i + 1).ports().get(0));

            } finally {
                if (jedis != null) {
                    jedis.close();
                }
            }
        }

        // connect N-node to first-node
        try {
            jedis = new Jedis(LOCAL_HOST, servers.get(servers.size() - 1).ports().get(0));
            jedis.clusterMeet(LOCAL_HOST, servers.get(0).ports().get(0));
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    private List<MasterNode> allocSlots() {
        int nodesCount = servers.size();
        int mastersCount = nodesCount / (numOfReplicates + 1);

        List<MasterNode> masters = new ArrayList<MasterNode>(mastersCount);

        // alloc slots on masters
        int slotPerNode = CLUSTER_HASH_SLOTS_NUMBER / mastersCount;
        int first = 0;
        double cursor = 0.0;
        for (int i = 0; i < mastersCount; i++) {
            int last = (int) Math.round(cursor + slotPerNode - 1);
            if (last > CLUSTER_HASH_SLOTS_NUMBER || i == mastersCount - 1) {
                last = CLUSTER_HASH_SLOTS_NUMBER - 1;
            }

            //Min step is 1.
            if (last < first)
                last = first;

            masters.add(new MasterNode(servers.get(i), new RealRedisCluster.SlotRange(first, last)));
            first = last + 1;
            cursor += slotPerNode;
        }

        int iter = 0;
        // Select N replicas for every master.
        for (int i = mastersCount; i < servers.size(); i++) {
            masters.get(iter).addSlave(servers.get(i));
            if (iter == mastersCount - 1) {
                iter = 0;
            } else {
                iter++;
            }
        }

        Jedis jedis = null;

        for (MasterNode master : masters) {
            try {
                //add slots
                jedis = new Jedis(LOCAL_HOST, master.getMaster().ports().get(0));
                jedis.clusterAddSlots(master.getSlotRange().getRange());
                //get node id
                String curNodeId = getNodeIdFromClusterNodesAck(jedis.clusterNodes());
                System.out.println(String.format("Master node: %s with slots[%d,%d]",
                        curNodeId,
                        master.getSlotRange().first,
                        master.getSlotRange().last));
                master.setNodeId(curNodeId);
            } finally {
                if (jedis != null) {
                    jedis.close();
                }
            }
        }
        return masters;
    }

    private String getNodeIdFromClusterNodesAck(String ack) {
        return ack.split(" :")[0];
    }

    private void setReplicates(List<MasterNode> masters) {
        for (MasterNode master : masters) {
            setSlaves(master.getNodeId(), master.getSlaves());
        }
    }

    private void setSlaves(String masterNodeId, Set<Redis> slaves) {
        Jedis jedis = null;
        for (Redis slave : slaves) {
            try {
                //add slots
                jedis = new Jedis(LOCAL_HOST, slave.ports().get(0));
                jedis.clusterReplicate(masterNodeId);
            } finally {
                if (jedis != null) {
                    jedis.close();
                }
            }
        }
    }

    private enum ClusterState {
        OK("ok"), FAIL("fail");
        private String state;

        ClusterState(String s) {
            state = s;
        }

        public String getState() {
            return state;
        }

        public static ClusterState getStateByStr(String s) {
            for (ClusterState clusterState : ClusterState.values()) {
                if (s.equals(clusterState.getState())) {
                    return clusterState;
                }
            }
            throw new IllegalStateException("illegal cluster state: " + s);
        }
    }

    private static class MasterNode {
        final Redis master;
        String nodeId;
        final RealRedisCluster.SlotRange slotRange;
        final Set<Redis> slaves;

        public MasterNode(Redis master, SlotRange slotRange) {
            this.master = master;
            this.slotRange = slotRange;
            slaves = new HashSet<Redis>();
        }

        public Set<Redis> getSlaves() {
            return slaves;
        }

        public void addSlave(Redis slave) {
            slaves.add(slave);
        }

        public Redis getMaster() {
            return master;
        }

        public RealRedisCluster.SlotRange getSlotRange() {
            return slotRange;
        }

        public String getNodeId() {
            return nodeId;
        }

        public void setNodeId(String nodeId) {
            this.nodeId = nodeId;
        }
    }

    private static class SlotRange {
        final int first;
        final int last;

        private SlotRange(int first, int last) {
            this.first = first;
            this.last = last;
        }

        public int[] getRange() {
            int[] range = new int[last - first + 1];
            for (int i = 0; i <= last - first; i++) {
                range[i] = first + i;
            }
            return range;
        }
    }
}