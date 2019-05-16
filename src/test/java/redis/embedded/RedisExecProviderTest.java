package redis.embedded;

import com.google.common.io.Resources;
import org.junit.Test;
import redis.embedded.exceptions.RedisBuildingException;
import redis.embedded.util.Architecture;
import redis.embedded.util.OS;

public class RedisExecProviderTest {

    private RedisServer redisServer;

    @Test
    public void shouldOverrideDefaultExecutable() throws Exception {
        RedisExecProvider customProvider = RedisExecProvider.defaultProvider()
            .override(OS.UNIX, Architecture.x86, Resources.getResource("redis-server-2.8.19-32").getFile())
            .override(OS.UNIX, Architecture.x86_64, Resources.getResource("redis-server-2.8.19").getFile())
            .override(OS.WINDOWS, Architecture.x86, Resources.getResource("redis-server-2.8.19.exe").getFile())
            .override(OS.WINDOWS, Architecture.x86_64, Resources.getResource("redis-server-2.8.19.exe").getFile())
            .override(OS.MAC_OS_X, Resources.getResource("redis-server-2.8.19").getFile());

        redisServer = new RedisServerBuilder()
            .redisExecProvider(customProvider)
            .build();
    }

    @Test(expected = RedisBuildingException.class)
    public void shouldFailWhenBadExecutableGiven() throws Exception {
        RedisExecProvider buggyProvider = RedisExecProvider.defaultProvider()
            .override(OS.UNIX, "some")
            .override(OS.WINDOWS, Architecture.x86, "some")
            .override(OS.WINDOWS, Architecture.x86_64, "some")
            .override(OS.MAC_OS_X, "some");

        redisServer = new RedisServerBuilder()
            .redisExecProvider(buggyProvider)
            .build();
    }

}