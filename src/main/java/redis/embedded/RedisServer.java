package redis.embedded;

import redis.embedded.util.OsArchitecture;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RedisServer extends AbstractRedisInstance {
    private static final String REDIS_4_READY_PATTERN = ".*Server initialized.*";
    private static final String REDIS_3_READY_PATTERN = ".*Server started.*";
    private static final int DEFAULT_REDIS_PORT = 6379;

    public RedisServer() throws IOException {
        this(DEFAULT_REDIS_PORT);
    }

    public RedisServer(int port) throws IOException {
        super(port);
        File executable = RedisExecProvider.defaultProvider().get();
        this.args = Arrays.asList(
                executable.getAbsolutePath(),
                "--port", Integer.toString(port)
        );
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

        OsArchitecture os = OsArchitecture.detect();
        if(os.equals(OsArchitecture.WINDOWS_x86) || os.equals(OsArchitecture.WINDOWS_x86_64)) {
            //In case of Windows, it use redis 3.0.XX
            //So it has different ready pattern.
            return REDIS_3_READY_PATTERN;
        } else {
            return REDIS_4_READY_PATTERN;
        }
    }
}
