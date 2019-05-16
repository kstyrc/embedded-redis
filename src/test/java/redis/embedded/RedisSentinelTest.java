package redis.embedded;

import com.google.common.collect.Sets;
import java.util.Arrays;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisSentinelPool;

import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;

@RunWith(Parameterized.class)
public class RedisSentinelTest {
    private String redisVersion;
    private RedisSentinel sentinel;
    private RedisServer server;

    @Parameterized.Parameters(name= "{index}: Redis version: {0}")
    public static Iterable<String> data() {
        return Arrays.asList("2.8", "3.0", "3.2", "4.0", "5.0");
    }

    public RedisSentinelTest(String redisVersion) {
        this.redisVersion = redisVersion;
    }

    @Test(timeout = 3000L)
    public void testSimpleRun() throws Exception {
        server = new RedisServer(RedisExecProvider.defaultProvider(redisVersion));
        sentinel = RedisSentinel.builder().redisExecProvider(RedisExecProvider.defaultProvider(redisVersion)).build();
        sentinel.start();
        server.start();
        TimeUnit.SECONDS.sleep(1);
        server.stop();
        sentinel.stop();
    }

    @Test
    public void shouldAllowSubsequentRuns() throws Exception {
        sentinel = RedisSentinel.builder().redisExecProvider(RedisExecProvider.defaultProvider(redisVersion)).build();
        sentinel.start();
        sentinel.stop();

        sentinel.start();
        sentinel.stop();

        sentinel.start();
        sentinel.stop();
    }

    @Test
    public void testSimpleOperationsAfterRun() throws Exception {
        //given
        server = new RedisServer(RedisExecProvider.defaultProvider(redisVersion));
        sentinel = RedisSentinel.builder().redisExecProvider(RedisExecProvider.defaultProvider(redisVersion)).build();
        server.start();
        sentinel.start();
        TimeUnit.SECONDS.sleep(1);

        //when
        try (JedisSentinelPool pool = new JedisSentinelPool("mymaster", Sets.newHashSet("localhost:26379"));
             Jedis jedis = pool.getResource()
        ) {
            jedis.mset("abc", "1", "def", "2");

            //then
            assertEquals("1", jedis.mget("abc").get(0));
            assertEquals("2", jedis.mget("def").get(0));
            assertEquals(null, jedis.mget("xyz").get(0));
        } finally {
            sentinel.stop();
            server.stop();
        }
    }

}