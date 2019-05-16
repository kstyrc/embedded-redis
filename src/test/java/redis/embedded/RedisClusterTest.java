package redis.embedded;

import com.google.common.collect.Sets;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisSentinelPool;
import redis.embedded.util.JedisUtil;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class RedisClusterTest {
    @Mock
    private Redis sentinel1;
    @Mock
    private Redis sentinel2;
    @Mock
    private Redis master1;
    @Mock
    private Redis master2;

    private RedisCluster instance;

    @Test
    public void stopShouldStopEntireCluster() throws Exception {
        //given
        final List<Redis> sentinels = Arrays.asList(sentinel1, sentinel2);
        final List<Redis> servers = Arrays.asList(master1, master2);
        instance = new RedisCluster(sentinels, servers);

        //when
        instance.stop();

        //then
        for(Redis s : sentinels) {
            verify(s).stop();
        }
        for(Redis s : servers) {
            verify(s).stop();
        }
    }

    @Test
    public void startShouldStartEntireCluster() throws Exception {
        //given
        final List<Redis> sentinels = Arrays.asList(sentinel1, sentinel2);
        final List<Redis> servers = Arrays.asList(master1, master2);
        instance = new RedisCluster(sentinels, servers);

        //when
        instance.start();

        //then
        for(Redis s : sentinels) {
            verify(s).start();
        }
        for(Redis s : servers) {
            verify(s).start();
        }
    }

    @Test
    public void isActiveShouldCheckEntireClusterIfAllActive() throws Exception {
        //given
        given(sentinel1.isActive()).willReturn(true);
        given(sentinel2.isActive()).willReturn(true);
        given(master1.isActive()).willReturn(true);
        given(master2.isActive()).willReturn(true);
        final List<Redis> sentinels = Arrays.asList(sentinel1, sentinel2);
        final List<Redis> servers = Arrays.asList(master1, master2);
        instance = new RedisCluster(sentinels, servers);

        //when
        instance.isActive();

        //then
        for(Redis s : sentinels) {
            verify(s).isActive();
        }
        for(Redis s : servers) {
            verify(s).isActive();
        }
    }

    @Test
    public void testSimpleOperationsAfterRunWithSingleMasterNoSlavesCluster() throws Exception {
        //given
        final RedisCluster cluster = RedisCluster.builder().sentinelCount(1).replicationGroup("ourmaster", 0).build();
        try {
            cluster.start();

            //when
            try (JedisSentinelPool pool = new JedisSentinelPool("ourmaster", Sets.newHashSet("localhost:26379"));
                 Jedis jedis = pool.getResource()
            ) {
                testPool(jedis);
            }
        } finally {
            cluster.stop();
        }
    }

    @Test
    public void testSimpleOperationsAfterRunWithSingleMasterAndOneSlave() throws Exception {
        //given
        final RedisCluster cluster = RedisCluster.builder().sentinelCount(1).replicationGroup("ourmaster", 1).build();
        try {
            cluster.start();

            //when
            try (JedisSentinelPool pool = new JedisSentinelPool("ourmaster", Sets.newHashSet("localhost:26379"));
                 Jedis jedis = pool.getResource()
            ) {
                testPool(jedis);
            }
        } finally {
            cluster.stop();
        }
    }

    @Test
    public void testSimpleOperationsAfterRunWithSingleMasterMultipleSlaves() throws Exception {
        //given
        final RedisCluster cluster = RedisCluster.builder().sentinelCount(1).replicationGroup("ourmaster", 2).build();
        try {
            cluster.start();

            //when
            try (JedisSentinelPool pool = new JedisSentinelPool("ourmaster", Sets.newHashSet("localhost:26379"));
                 Jedis jedis = pool.getResource()
            ) {
                testPool(jedis);
            }
        } finally {
            cluster.stop();
        }
    }

    @Test
    public void testSimpleOperationsAfterRunWithTwoSentinelsSingleMasterMultipleSlaves() throws Exception {
        //given
        final RedisCluster cluster = RedisCluster.builder().sentinelCount(2).replicationGroup("ourmaster", 2).build();
        try {
            cluster.start();

            //when
            try (JedisSentinelPool pool = new JedisSentinelPool("ourmaster", Sets.newHashSet("localhost:26379", "localhost:26380"));
                 Jedis jedis = pool.getResource()
            ) {
                testPool(jedis);
            }
        } finally {
            cluster.stop();
        }
    }

    @Test
    public void testSimpleOperationsAfterRunWithTwoPredefinedSentinelsSingleMasterMultipleSlaves() throws Exception {
        //given
        List<Integer> sentinelPorts = Arrays.asList(26381, 26382);
        final RedisCluster cluster = RedisCluster.builder().sentinelPorts(sentinelPorts).replicationGroup("ourmaster", 2).build();
        try {
            cluster.start();
            final Set<String> sentinelHosts = JedisUtil.portsToJedisHosts(sentinelPorts);

            //when
            try (JedisSentinelPool pool = new JedisSentinelPool("ourmaster", sentinelHosts);
                 Jedis jedis = pool.getResource()
            ) {
                testPool(jedis);
            }
        } finally {
            cluster.stop();
        }
    }

    @Test
    public void testSimpleOperationsAfterRunWithThreeSentinelsThreeMastersOneSlavePerMasterCluster() throws Exception {
        //given
        final String master1 = "master1";
        final String master2 = "master2";
        final String master3 = "master3";
        final RedisCluster cluster = RedisCluster.builder().sentinelCount(3).quorumSize(2)
            .replicationGroup(master1, 1)
            .replicationGroup(master2, 1)
            .replicationGroup(master3, 1)
            .build();
        try {
            cluster.start();

            //when
            try (JedisSentinelPool pool1 = new JedisSentinelPool(master1, Sets.newHashSet("localhost:26379", "localhost:26380", "localhost:26381"));
                 JedisSentinelPool pool2 = new JedisSentinelPool(master2, Sets.newHashSet("localhost:26379", "localhost:26380", "localhost:26381"));
                 JedisSentinelPool pool3 = new JedisSentinelPool(master3, Sets.newHashSet("localhost:26379", "localhost:26380", "localhost:26381"));
                 Jedis jedis1 = pool1.getResource();
                 Jedis jedis2 = pool2.getResource();
                 Jedis jedis3 = pool3.getResource()
            ) {
                testPool(jedis1);
                testPool(jedis2);
                testPool(jedis3);
            }
        } finally {
            cluster.stop();
        }
    }

    @Test
    public void testSimpleOperationsAfterRunWithThreeSentinelsThreeMastersOneSlavePerMasterEphemeralCluster() throws Exception {
        //given
        final String master1 = "master1";
        final String master2 = "master2";
        final String master3 = "master3";
        final RedisCluster cluster = RedisCluster.builder().ephemeral().sentinelCount(3).quorumSize(2)
                .replicationGroup(master1, 1)
                .replicationGroup(master2, 1)
                .replicationGroup(master3, 1)
                .build();
        try {
            cluster.start();
            final Set<String> sentinelHosts = JedisUtil.sentinelHosts(cluster);

            //when
            try (JedisSentinelPool pool1 = new JedisSentinelPool(master1, sentinelHosts);
                 JedisSentinelPool pool2 = new JedisSentinelPool(master2, sentinelHosts);
                 JedisSentinelPool pool3 = new JedisSentinelPool(master3, sentinelHosts);
                 Jedis jedis1 = pool1.getResource();
                 Jedis jedis2 = pool2.getResource();
                 Jedis jedis3 = pool3.getResource()
            ) {
                testPool(jedis1);
                testPool(jedis2);
                testPool(jedis3);
            }
        } finally {
            cluster.stop();
        }
    }

    private void testPool(Jedis jedis) {
        jedis.mset("abc", "1", "def", "2");

        //then
        assertEquals("1", jedis.mget("abc").get(0));
        assertEquals("2", jedis.mget("def").get(0));
        assertEquals(null, jedis.mget("xyz").get(0));
    }
}