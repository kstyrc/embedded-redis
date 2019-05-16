package redis.embedded;

import java.util.Arrays;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import static org.junit.Assert.*;

@RunWith(Parameterized.class)
public class RedisServerTest {

	private String redisVersion;
	private RedisServer redisServer;

	@Parameterized.Parameters(name= "{index}: Redis version: {0}")
	public static Iterable<String> data() {
		return Arrays.asList("2.8", "3.0", "3.2", "4.0", "5.0");
	}

	public RedisServerTest(String redisVersion) {
		this.redisVersion = redisVersion;
	}

	@Test(timeout = 1500L)
	public void testSimpleRun() throws Exception {
		redisServer = new RedisServer(RedisExecProvider.defaultProvider(redisVersion), 6379);
		redisServer.start();
		Thread.sleep(1000L);
		redisServer.stop();
	}
	
	@Test(expected = RuntimeException.class)
	public void shouldNotAllowMultipleRunsWithoutStop() throws Exception {
		try {
			redisServer = new RedisServer(RedisExecProvider.defaultProvider(redisVersion), 6379);
			redisServer.start();
			redisServer.start();
		} finally {
			redisServer.stop();
		}
	}
	
	@Test
	public void shouldAllowSubsequentRuns() throws Exception {
		redisServer = new RedisServer(RedisExecProvider.defaultProvider(redisVersion), 6379);
		redisServer.start();
		redisServer.stop();
		
		redisServer.start();
		redisServer.stop();
		
		redisServer.start();
		redisServer.stop();
	}
	
	@Test
	public void testSimpleOperationsAfterRun() throws Exception {
		redisServer = new RedisServer(RedisExecProvider.defaultProvider(redisVersion), 6379);
		redisServer.start();

		try (JedisPool pool = new JedisPool("localhost", 6379);
			 Jedis jedis = pool.getResource()
		) {
			jedis.mset("abc", "1", "def", "2");

			assertEquals("1", jedis.mget("abc").get(0));
			assertEquals("2", jedis.mget("def").get(0));
			assertEquals(null, jedis.mget("xyz").get(0));
		} finally {
			redisServer.stop();
		}
	}

    @Test
    public void shouldIndicateInactiveBeforeStart() throws Exception {
        redisServer = new RedisServer(RedisExecProvider.defaultProvider(redisVersion), 6379);
        assertFalse(redisServer.isActive());
    }

    @Test
    public void shouldIndicateActiveAfterStart() throws Exception {
        redisServer = new RedisServer(RedisExecProvider.defaultProvider(redisVersion), 6379);
        redisServer.start();
        assertTrue(redisServer.isActive());
        redisServer.stop();
    }

    @Test
    public void shouldIndicateInactiveAfterStop() throws Exception {
        redisServer = new RedisServer(RedisExecProvider.defaultProvider(redisVersion), 6379);
        redisServer.start();
        redisServer.stop();
        assertFalse(redisServer.isActive());
    }
}
