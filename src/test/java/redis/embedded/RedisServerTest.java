package redis.embedded;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

public class RedisServerTest {

	private RedisServer redisServer;
	
	@Test(timeout = 1500L)
	public void testSimpleRun() throws Exception {
		redisServer = new RedisServer(6379);
		redisServer.start();
		Thread.sleep(1000L);
		redisServer.stop();
	}
	
	@Test(expected = RuntimeException.class)
	public void shouldNotAllowMultipleRunsWithoutStop() throws Exception {
		redisServer = new RedisServer(6379);
		redisServer.start();
		redisServer.start();
	}
	
	@Test
	public void shouldAllowSubsequentRuns() throws Exception {
		redisServer = new RedisServer(6379);
		redisServer.start();
		redisServer.stop();
		
		Thread.sleep(200L);
		redisServer.start();
		redisServer.stop();
		
		Thread.sleep(200L);
		redisServer.start();
		redisServer.stop();
	}
	
	@Test
	public void testSimpleOperationsAfterRun() throws Exception {
		redisServer = new RedisServer(6379);
		redisServer.start();
		
		JedisPool pool = null;
		Jedis jedis = null;
		try {
			pool = new JedisPool("localhost", 6379);
			jedis = pool.getResource();
			jedis.mset("abc", "1", "def", "2");
			
			assertEquals("1", jedis.mget("abc").get(0));
			assertEquals("2", jedis.mget("def").get(0));
			assertEquals(null, jedis.mget("xyz").get(0));
			pool.returnResource(jedis);
		} finally {
			if (jedis != null)
				pool.returnResource(jedis);
			redisServer.stop();
		}
	}
}
