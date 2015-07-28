package redis.embedded.cluster;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.Protocol;
import redis.embedded.Redis;
import redis.embedded.RedisExecProvider;
import redis.embedded.RedisServerBuilder;
import redis.embedded.exceptions.EmbeddedRedisException;
import redis.embedded.util.OS;

import java.util.*;

import static junit.framework.Assert.fail;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

/**
 * Created by dragan on 17.07.15.
 */
public class RealRedisClusterTest {
    private Redis master1;
    private Redis master2;
    private Redis master3;
    private static final int DEFAULT_REPLICATES = 1;
    private static final int DEFAULT_NUMBER_RETRIES = 5;
    private static final int CONNECTION_TIMEOUT = Protocol.DEFAULT_TIMEOUT;
    private static final Collection<Integer> ports = Arrays.asList(3000, 3001, 3002, 3003);
    private static final String LOCAL_HOST = "127.0.0.1";

    private RedisExecProvider redisExecProvider;
    private Redis instance;

    @Before
    public void setUp() throws Exception {
        master1 = mock(Redis.class);
        master2 = mock(Redis.class);
        master3 = mock(Redis.class);

        redisExecProvider = RedisExecProvider.defaultProvider();
        redisExecProvider.override(OS.UNIX, "redis-server-3.0.0");
    }

    @Test
    public void numberOfNodeShouldBeMoreThanThree() throws Exception {
        //given
        final List<Redis> oneServer = Arrays.asList(master1);
        //when
        try {
            instance = new RealRedisCluster(oneServer, DEFAULT_REPLICATES, DEFAULT_NUMBER_RETRIES, CONNECTION_TIMEOUT);
            fail();
        } catch (EmbeddedRedisException e) {
            assertThat(e.getMessage(), equalTo("Redis Cluster requires at least 3 master nodes."));
        }

        final List<Redis> twoServers = Arrays.asList(master1, master2);
        try {
            instance = new RealRedisCluster(twoServers, DEFAULT_REPLICATES, DEFAULT_NUMBER_RETRIES, CONNECTION_TIMEOUT);
            fail();
        } catch (EmbeddedRedisException e) {
            assertThat(e.getMessage(), equalTo("Redis Cluster requires at least 3 master nodes."));
        }
    }

    @Test
    public void numberOfReplicatesShouldBeMoreThatOne() throws Exception {
        final List<Redis> threeServers = Arrays.asList(master1, master2, master3);
        try {
            instance = new RealRedisCluster(threeServers, 0, DEFAULT_NUMBER_RETRIES, CONNECTION_TIMEOUT);
            fail();
        } catch (EmbeddedRedisException e) {
            assertThat(e.getMessage(), equalTo("Redis Cluster requires at least 1 replication."));
        }
    }

    @Test
    public void numberOfReplicatesShouldBeLessThanNumberOfServers() throws Exception {
        final List<Redis> threeServers = Arrays.asList(master1, master2, master3);
        try {
            instance = new RealRedisCluster(threeServers, 10, DEFAULT_NUMBER_RETRIES, CONNECTION_TIMEOUT);
            fail();
        } catch (EmbeddedRedisException e) {
            assertThat(e.getMessage(), equalTo("Redis Cluster requires number of replications less than (number of nodes - 1)."));
        }
    }

    @Test
    public void numberOfRetriesShouldBeMoreThanZero() throws Exception {
        final List<Redis> threeServers = Arrays.asList(master1, master2, master3);
        try {
            instance = new RealRedisCluster(threeServers, DEFAULT_REPLICATES, 0, CONNECTION_TIMEOUT);
            fail();
        } catch (EmbeddedRedisException e) {
            assertThat(e.getMessage(), equalTo("Redis Cluster requires number of retries more than zero."));
        }
    }

    @Test
    public void isActiveShouldCheckEntireClusterIfAllActive() throws Exception {
        //given
        instance = new RealRedisClusterBuilder()
                .withServerBuilder(new RedisServerBuilder()
                        .redisExecProvider(redisExecProvider))
                .serverPorts(ports).build();
        //when
        instance.start();
        boolean isActive = instance.isActive();

        //then
        assertThat(isActive, equalTo(true));
    }

    @Test
    public void startShouldStartEntireCluster() throws Exception {
        //given
        instance = new RealRedisClusterBuilder()
                .withServerBuilder(new RedisServerBuilder()
                        .redisExecProvider(redisExecProvider))
                .serverPorts(ports).build();

        Set<HostAndPort> hostAndPorts = new HashSet<HostAndPort>(ports.size());
        for (Integer port : ports) {
            hostAndPorts.add(new HostAndPort(LOCAL_HOST, port));
        }

        //when
        JedisCluster jc = null;
        try {
            instance.start();
            jc = new JedisCluster(hostAndPorts);
            jc.hset("key", "field", "value");
            String val = jc.hget("key", "field");
            assertThat(val, equalTo("value"));
        } finally {
            if (jc != null) {
                jc.close();
            }
        }
    }

    @After
    public void after() {
        if (instance != null) {
            instance.stop();
        }
    }
}
