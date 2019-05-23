package redis.embedded;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RedisServer extends AbstractRedisInstance {
    static final String REDIS_STANDALONE_READY_PATTERN = ".*The server is now ready to accept connections on port.*";
    static final int DEFAULT_REDIS_PORT = 6379;

    public RedisServer() throws IOException {
        this(DEFAULT_REDIS_PORT);
    }

    public RedisServer(int port) throws IOException {
        super(port, REDIS_STANDALONE_READY_PATTERN);
        File executable = RedisExecProvider.defaultProvider().get();
        this.args = Arrays.asList(
                executable.getAbsolutePath(),
                "--port", Integer.toString(port)
        );
	}

    public RedisServer(File executable, int port) {
        super(port, REDIS_STANDALONE_READY_PATTERN);
        this.args = Arrays.asList(
                executable.getAbsolutePath(),
                "--port", Integer.toString(port)
        );
    }

    public RedisServer(RedisExecProvider redisExecProvider, int port) throws IOException {
        super(port, REDIS_STANDALONE_READY_PATTERN);
        this.args = Arrays.asList(
                redisExecProvider.get().getAbsolutePath(),
                "--port", Integer.toString(port)
        );
    }

    public RedisServer(List<String> args, int port) {
        this(args, port, REDIS_STANDALONE_READY_PATTERN);
    }

    public RedisServer(List<String> args, int port, String readyPattern) {
        super(port, readyPattern);
        this.args = new ArrayList<String>(args);
    }

    public static RedisServerBuilder builder() {
        return new RedisServerBuilder();
    }

}
