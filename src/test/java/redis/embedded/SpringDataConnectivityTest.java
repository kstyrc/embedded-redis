package redis.embedded;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import redis.clients.jedis.JedisShardInfo;

import static org.junit.Assert.assertEquals;

public class SpringDataConnectivityTest {

    private RedisServer redisServer;
    private RedisTemplate<String, String> template;
    private JedisConnectionFactory connectionFactory;

    @Before
    public void setUp() throws Exception {
        redisServer = new RedisServer(6379);
        redisServer.start();

        JedisShardInfo shardInfo = new JedisShardInfo("localhost", 6379);
        connectionFactory = new JedisConnectionFactory();
        connectionFactory.setShardInfo(shardInfo);

        template = new StringRedisTemplate();
        template.setConnectionFactory(connectionFactory);
        template.afterPropertiesSet();
    }

    @Test
    public void shouldBeAbleToUseSpringData() throws Exception {
        // given
        template.opsForValue().set("foo", "bar");

        // when
        String result = template.opsForValue().get("foo");

        // then
        assertEquals("bar", result);
    }

    @After
    public void tearDown() throws Exception {
        redisServer.stop();
    }
}
