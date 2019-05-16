package redis.embedded;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RedisServer extends AbstractRedisInstance {
    /**
     * Redis < 4.0 outputs "The server is now ready to accept connections on port"
     * Redis 4.0+ outputs "Ready to accept connections"
     */
    private static final String REDIS_READY_PATTERN = ".*(The server is now ready to accept connections on port|Ready to accept connections).*";
    private static final int DEFAULT_REDIS_PORT = 6379;

    public RedisServer() throws IOException {
        this(RedisExecProvider.defaultProvider(), DEFAULT_REDIS_PORT);
    }

    public RedisServer(int port) throws IOException {
        this(RedisExecProvider.defaultProvider(), port);
    }

    public RedisServer(RedisExecProvider redisExecProvider) throws IOException {
        this(redisExecProvider, DEFAULT_REDIS_PORT);
    }

    public RedisServer(File executable, int port) {
        super(port);
        this.args = Arrays.asList(
                executable.getAbsolutePath(),
                "--port", Integer.toString(port)
        );
    }

    public RedisServer(RedisExecProvider redisExecProvider, int port) throws IOException {
        super(port);
        this.args = Arrays.asList(
                redisExecProvider.get().getAbsolutePath(),
                "--port", Integer.toString(port)
        );
    }

    RedisServer(List<String> args, int port) {
        super(port);
        this.args = new ArrayList<>(args);
    }

    public static RedisServerBuilder builder() {
        return new RedisServerBuilder();
    }

    @Override
    protected String redisReadyPattern() {
        return REDIS_READY_PATTERN;
    }
}
