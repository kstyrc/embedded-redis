package redis.embedded;

import java.io.*;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by piotrturek on 22/01/15.
 */
abstract class AbstractRedisInstance implements RedisInstance {
    protected List<String> args = Collections.emptyList();
    private volatile boolean active = false;
	private Process redisProcess;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

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
        logErrors();
        awaitRedisServerReady();
        active = true;
    }

    private void logErrors() {
        final InputStream errorStream = redisProcess.getErrorStream();
        executor.submit(() -> {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(errorStream))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println(line);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

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
            } while (!outputLine.matches(redisReadyPattern()));
        }
    }

    protected abstract String redisReadyPattern();

    private ProcessBuilder createRedisProcessBuilder() {
        File executable = new File(args.get(0));
        ProcessBuilder pb = new ProcessBuilder(args);
        pb.directory(executable.getParentFile());
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
