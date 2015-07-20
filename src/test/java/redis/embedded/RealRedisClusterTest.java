package redis.embedded;

import org.junit.Before;
import org.junit.Test;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisCluster;
import redis.embedded.exceptions.EmbeddedRedisException;
import redis.embedded.util.OS;

import java.util.*;

import static junit.framework.Assert.fail;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * Created by dragan on 17.07.15.
 */
public class RealRedisClusterTest {
    private Redis master1;
    private Redis master2;
    private Redis master3;

    private RealRedisCluster instance;

    @Before
    public void setUp() throws Exception {
        master1 = mock(Redis.class);
        master2 = mock(Redis.class);
        master3 = mock(Redis.class);
    }

    @Test
    public void startShouldStartEntireCluster() throws Exception {
        //given
        final List<Redis> servers = Arrays.asList(master1, master2, master3);
        instance = new RealRedisCluster(servers);

        //when
        instance.create();

        //then
        for (Redis s : servers) {
            verify(s).start();
        }
    }

    @Test
    public void stopShouldStopEntireCluster() throws Exception {
        //given
        final List<Redis> servers = Arrays.asList(master1, master2, master3);
        instance = new RealRedisCluster(servers);

        //when
        instance.stop();

        //then
        for (Redis s : servers) {
            verify(s).stop();
        }
    }

    @Test
    public void numberOfNodeShouldBeMoreThanThree() throws Exception {
        //given
        final List<Redis> oneServer = Arrays.asList(master1);
        //when
        try {
            instance = new RealRedisCluster(oneServer);
            fail();
        } catch (EmbeddedRedisException e) {
            assertThat(e.getMessage(), equalTo("Redis Cluster requires at least 3 master nodes."));
        }

        final List<Redis> twoServers = Arrays.asList(master1, master2);
        try {
            instance = new RealRedisCluster(twoServers);
            fail();
        } catch (EmbeddedRedisException e) {
            assertThat(e.getMessage(), equalTo("Redis Cluster requires at least 3 master nodes."));
        }
    }

    @Test
    public void numberOfReplicatesShouldBeMoreThatOne() throws Exception {
        final List<Redis> threeServers = Arrays.asList(master1, master2, master3);
        try {
            instance = new RealRedisCluster(threeServers, 0);
            fail();
        } catch (EmbeddedRedisException e) {
            assertThat(e.getMessage(), equalTo("Redis Cluster requires at least 1 replication."));
        }
    }

    @Test
    public void numberOfReplicatesShouldBeLessThanNumberOfServers() throws Exception {
        final List<Redis> threeServers = Arrays.asList(master1, master2, master3);
        try {
            instance = new RealRedisCluster(threeServers, 10);
            fail();
        } catch (EmbeddedRedisException e) {
            assertThat(e.getMessage(), equalTo("Redis Cluster requires number of replications less than number of nodes - 1."));
        }
    }
    @Test
    public void isActiveShouldCheckEntireClusterIfAllActive() throws Exception {
        //given
        given(master1.isActive()).willReturn(true);
        given(master2.isActive()).willReturn(true);
        given(master3.isActive()).willReturn(true);
        final List<Redis> servers = Arrays.asList(master1, master2, master3);
        instance = new RealRedisCluster(servers);

        //when
        instance.create();
        boolean isActive = instance.isActive();

        //then
        for (Redis s : servers) {
            verify(s).isActive();
        }
        assertThat(isActive, equalTo(true));

    }

    @Test
    public void createShouldStartEntireCluster() throws Exception {
        //given
        Collection<Integer> ports = Arrays.asList(3000, 3001, 3002, 3003);

        RedisExecProvider redisExecProvider = RedisExecProvider.defaultProvider();
        redisExecProvider.override(OS.UNIX, "redis-server-3.0.0");
        instance = new RealRedisClusterBuilder()
                .withServerBuilder(new RedisServerBuilder()
                        .redisExecProvider(redisExecProvider))
                .serverPorts(ports).build();
        Set<HostAndPort> hostAndPorts = new HashSet<HostAndPort>(ports.size());
        for (Integer port : ports) {
            hostAndPorts.add(new HostAndPort("127.0.0.1", port));
        }
        String val = null;
        //when
        try {
            instance.create();
            instance.start();
            JedisCluster jc = new JedisCluster(hostAndPorts);
            jc.hset("key", "field", "value");
            val = jc.hget("key", "field");
            assertThat(val, equalTo("value"));
        } finally {
            instance.stop();
        }
    }
}
