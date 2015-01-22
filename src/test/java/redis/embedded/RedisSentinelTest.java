package redis.embedded;

import org.junit.Test;

public class RedisSentinelTest {
    private RedisSentinel sentinel;
    private RedisServer server;

    @Test(timeout = 3000L)
    public void testSimpleRun() throws Exception {
        server = new RedisServer();
        sentinel = RedisSentinel.builder().build();
        sentinel.start();
        server.start();
        Thread.sleep(1000L);
        server.stop();
        sentinel.stop();
    }

}