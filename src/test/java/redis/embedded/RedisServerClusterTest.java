package redis.embedded;

import java.util.Arrays;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import static org.junit.Assert.assertEquals;

@RunWith(Parameterized.class)
public class RedisServerClusterTest {

    private String redisVersion;

    private RedisServer redisServer1;
    private RedisServer redisServer2;

    @Parameterized.Parameters(name= "{index}: Redis version: {0}")
    public static Iterable<String> data() {
        return Arrays.asList("2.8", "3.0", "3.2", "4.0", "5.0");
    }

    public RedisServerClusterTest(String redisVersion) {
        this.redisVersion = redisVersion;
    }

    @Before
    public void setUp() throws Exception {
        redisServer1 = RedisServer.builder()
                .redisExecProvider(RedisExecProvider.defaultProvider(redisVersion))
                .port(6300)
                .build();

        redisServer2 = RedisServer.builder()
                .redisExecProvider(RedisExecProvider.defaultProvider(redisVersion))
                .port(6301)
                .slaveOf("localhost", 6300)
                .build();

        redisServer1.start();
        redisServer2.start();
    }

    @Test
    public void testSimpleOperationsAfterRun() throws Exception {
        JedisPool pool = new JedisPool("localhost", 6300);
        try (Jedis jedis = pool.getResource()) {
            jedis.mset("abc", "1", "def", "2");

            assertEquals("1", jedis.mget("abc").get(0));
            assertEquals("2", jedis.mget("def").get(0));
            assertEquals(null, jedis.mget("xyz").get(0));
        }
    }


    @After
    public void tearDown() throws Exception {
        redisServer1.stop();
        redisServer2.stop();
    }
}
