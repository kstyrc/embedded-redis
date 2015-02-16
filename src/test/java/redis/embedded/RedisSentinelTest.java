package redis.embedded;

import com.google.common.collect.Sets;
import org.junit.Test;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisSentinelPool;

import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;

public class RedisSentinelTest {
    private RedisSentinel sentinel;
    private RedisServer server;

    @Test(timeout = 3000L)
    public void testSimpleRun() throws Exception {
        server = new RedisServer();
        sentinel = RedisSentinel.builder().build();
        sentinel.start();
        server.start();
        TimeUnit.SECONDS.sleep(1);
        server.stop();
        sentinel.stop();
    }

    @Test
    public void shouldAllowSubsequentRuns() throws Exception {
        sentinel = RedisSentinel.builder().build();
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
        server = new RedisServer();
        sentinel = RedisSentinel.builder().build();
        server.start();
        sentinel.start();
        TimeUnit.SECONDS.sleep(1);

        //when
        JedisSentinelPool pool = null;
        Jedis jedis = null;
        try {
            pool = new JedisSentinelPool("mymaster", Sets.newHashSet("localhost:26379"));
            jedis = pool.getResource();
            jedis.mset("abc", "1", "def", "2");

            //then
            assertEquals("1", jedis.mget("abc").get(0));
            assertEquals("2", jedis.mget("def").get(0));
            assertEquals(null, jedis.mget("xyz").get(0));
        } finally {
            if (jedis != null)
                pool.returnResource(jedis);
            sentinel.stop();
            server.stop();
        }
    }

}