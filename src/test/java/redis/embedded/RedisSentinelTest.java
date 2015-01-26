package redis.embedded;

import org.junit.Test;

import java.util.concurrent.TimeUnit;

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

}