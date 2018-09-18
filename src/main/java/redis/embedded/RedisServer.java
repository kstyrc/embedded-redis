package redis.embedded;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RedisServer extends AbstractRedisInstance {
    private static final String REDIS_READY_PATTERN = ".*(R|r)eady to accept connections.*";
    private static final int DEFAULT_REDIS_PORT = 6379;

    public RedisServer() {
        this(DEFAULT_REDIS_PORT);
    }

    public RedisServer(int port) {
        super(port);
        this.args = builder().port(port).build().args;
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
        this.args = new ArrayList<String>(args);
    }

    public static RedisServerBuilder builder() {
        return new RedisServerBuilder();
    }

    @Override
    protected String redisReadyPattern() {
        return REDIS_READY_PATTERN;
    }
}
