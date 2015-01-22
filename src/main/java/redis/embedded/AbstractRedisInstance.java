package redis.embedded;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.List;

/**
 * Created by piotrturek on 22/01/15.
 */
abstract class AbstractRedisInstance implements RedisInstance {
	private static final String REDIS_READY_PATTERN = ".*The server is now ready to accept connections on port.*";
    protected List<String> args = Collections.emptyList();
    private volatile boolean active = false;
	private Process redisProcess;

	@Override
    public boolean isActive() {
        return active;
    }

	@Override
    public synchronized void start() throws IOException {
        if (active) {
            throw new RuntimeException("This redis server instance is already running...");
        }
        redisProcess = createRedisProcessBuilder().start();
        awaitRedisServerReady();
        active = true;
    }

	private void awaitRedisServerReady() throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(redisProcess.getInputStream()))) {
            String outputLine;
            do {
                outputLine = reader.readLine();

                if (outputLine == null) {
                    //Something goes wrong. Stream is ended before server was activated.
                    throw new RuntimeException("Can't start redis server. Check logs for details.");
                }
            } while (!outputLine.matches(REDIS_READY_PATTERN));
        }
    }

	private ProcessBuilder createRedisProcessBuilder() {
        File executable = new File(args.get(0));
        ProcessBuilder pb = new ProcessBuilder(args);
        pb.directory(executable.getParentFile());
        pb.redirectErrorStream();
        return pb;
    }

    @Override
    public synchronized void stop() throws InterruptedException {
        if (active) {
            redisProcess.destroy();
            redisProcess.waitFor();
            active = false;
        }
    }
}
