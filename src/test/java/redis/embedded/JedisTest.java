package redis.embedded;

import org.junit.Test;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCluster;

import java.util.HashSet;
import java.util.Set;

public class JedisTest {

    @Test
    public void basicRun() throws Exception {
//        Jedis jedis = new Jedis("localhost", 6667);
//        String value = jedis.get("key");
//        System.out.println(value);
    }

    @Test
    public void executeAndBuildCluster() throws Exception {


        final RedisCluster cluster = RedisCluster.builder().sentinelCount(3).quorumSize(2)
                .replicationGroup("master1", 1)
                .replicationGroup("master2", 1)
                .replicationGroup("master3", 1)
                .build();
        cluster.start();

        RedisExecProvider customRedisProvider = RedisExecProvider.defaultProvider();

        RedisServer redisServer = RedisServer.builder()
                .redisExecProvider(customRedisProvider)
                .port(7000)
                .slaveOf("localhost", 6379)
//				.configFile("/path/to/your/redis.conf")
                .setting("daemonize no")
                .setting("appendonly no")
//				.setting("maxheap 128M")
                .build();

        redisServer.start();

        redisServer.stop();
        cluster.stop();
    }

//    @Test
    public void getFromClusterTest() throws Exception {


        Jedis node1 = new Jedis("localhost", 7000);
        Jedis node2 = new Jedis("localhost", 7001);
        Jedis node3 = new Jedis("localhost", 7002);

        node1.clusterMeet("127.0.0.1", 7001);
        node1.clusterMeet("127.0.0.1", 7002);

        // split available slots across the three nodes
        int slotsPerNode = JedisCluster.HASHSLOTS / 3;
        int[] node1Slots = new int[slotsPerNode];
        int[] node2Slots = new int[slotsPerNode + 1];
        int[] node3Slots = new int[slotsPerNode];
        for (int i = 0, slot1 = 0, slot2 = 0, slot3 = 0; i < JedisCluster.HASHSLOTS; i++) {
            if (i < slotsPerNode) {
                node1Slots[slot1++] = i;
            } else if (i > slotsPerNode * 2) {
                node3Slots[slot3++] = i;
            } else {
                node2Slots[slot2++] = i;
            }
        }

        node1.clusterAddSlots(node1Slots);
        node2.clusterAddSlots(node2Slots);
        node3.clusterAddSlots(node3Slots);

        waitForClusterReady(node1, node2, node3);

        Set<HostAndPort> jedisClusterNodes = new HashSet<HostAndPort>();
        //Jedis Cluster will attempt to discover cluster nodes automatically
        jedisClusterNodes.add(new HostAndPort("127.0.0.1", 7000));
        JedisCluster jc = new JedisCluster(jedisClusterNodes);
        jc.set("foo", "bar");
        String value = jc.get("foo");
        System.out.println(value);
    }

    public void waitForClusterReady(Jedis... nodes) throws InterruptedException {
        boolean clusterOk = false;
        while (!clusterOk) {
            boolean isOk = true;
            for (Jedis node : nodes) {
                if (!node.clusterInfo().split("\n")[0].contains("ok")) {
                    isOk = false;
                    break;
                }
            }

            if (isOk) {
                clusterOk = true;
            }

            Thread.sleep(50);
        }
    }
}
