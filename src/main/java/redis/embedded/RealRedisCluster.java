package redis.embedded;

import redis.clients.jedis.Jedis;
import redis.embedded.exceptions.EmbeddedRedisException;

import java.util.*;

/**
 * Created by dragan on 17.07.15.
 */
public class RealRedisCluster implements Cluster {
    private final List<Redis> servers = new LinkedList<Redis>();
    private static final int CLUSTER_HASH_SLOTS_NUMBER = 16384;

    RealRedisCluster(List<Redis> servers) {
        if (servers.size() > 2) {
            this.servers.addAll(servers);
        } else {
            throw new EmbeddedRedisException("Redis Cluster requires at least 3 master nodes.");
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
        allocSlots();
        joinCluster();

        System.out.println("Waiting for the cluster to join");
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new EmbeddedRedisException(e.getMessage(), e);
        }
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

    private void joinCluster() {
        Jedis jedis = null;

        //connect sequentially nodes
        for (int i = 0; i < servers.size() - 1; i++) {
            try {
                jedis = new Jedis("127.0.0.1", servers.get(i).ports().get(0));
                String ack = jedis.clusterMeet("127.0.0.1", servers.get(i + 1).ports().get(0));
                if (i == servers.size() - 2) {
                    // connect N node to 1 node
                    jedis = new Jedis("127.0.0.1", servers.get(servers.size() - 1).ports().get(0));
                    ack = jedis.clusterMeet("127.0.0.1", servers.get(0).ports().get(0));
                }
            } finally {
                if (jedis != null) {
                    jedis.close();
                }
            }
        }
    }

    private void allocSlots() {
        int nodesCount = servers.size();
        //TODO
        // num replics
        int numReplicas = 1;
        int mastersCount = nodesCount / (numReplicas + 1);

        List<Node> masters = new ArrayList<Node>(mastersCount);

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

            masters.add(new Node(servers.get(i), new RealRedisCluster.SlotRange(first, last)));

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

        for (Node master : masters) {
            try {
                jedis = new Jedis("127.0.0.1", master.getMaster().ports().get(0));
                jedis.clusterAddSlots(master.getSlotRange().getRange());
            } finally {
                if (jedis != null) {
                    jedis.close();
                }
            }
        }
    }

    private static class Node {
        final Redis master;
        final RealRedisCluster.SlotRange slotRange;
        final Set<Redis> slaves;

        public Node(Redis master, RealRedisCluster.SlotRange slotRange) {
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
    }

    private static class SlotRange {
        final int first;
        final int last;

        private SlotRange(int first, int last) {
            this.first = first;
            this.last = last;
        }

        public int[] getRange() {
            int[] range = new int[last - first];
            for (int i = 0; i < last - first; i++) {
                range[i] = first + i;
            }
            return range;
        }
    }
}