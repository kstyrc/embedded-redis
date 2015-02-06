package redis.embedded;

import java.io.IOException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import redis.clients.jedis.Jedis;

public class RedisSentinelTest {

    private RedisServer sentinel;
    private RedisServer master;
    
    @Before
    public void setUp() throws IOException {
        sentinel = RedisServer.builder()
                .port(26300)
                .sentinel()
                .build();
        master = RedisServer.builder()
                .port(6300)
                .build();
        sentinel.start();
        master.start();
    }
    
    @Test
    public void test() throws InterruptedException {
        Jedis jedis = new Jedis(sentinel.getHost(), sentinel.getPort());
        try {
            Assert.assertTrue(jedis.sentinelMasters().isEmpty());
            jedis.sentinelMonitor("master", master.getHost(), master.getPort(), 1);
            Assert.assertEquals(1, jedis.sentinelMasters().size());
        } finally {
            jedis.close();
        }
    }
    
    @After
    public void tearDown() throws InterruptedException {
        sentinel.stop();
        master.stop();
    }
}
