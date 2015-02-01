package redis.embedded;

import redis.embedded.exceptions.EmbeddedRedisException;

import java.io.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by piotrturek on 22/01/15.
 */
abstract class AbstractRedisInstance implements Redis {
    protected List<String> args = Collections.emptyList();
    private volatile boolean active = false;
	private Process redisProcess;
    private final int port;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    protected AbstractRedisInstance(int port) {
        this.port = port;
    }

    @Override
    public boolean isActive() {
        return active;
    }

	@Override
    public synchronized void start() throws EmbeddedRedisException {
        if (active) {
            throw new EmbeddedRedisException("This redis server instance is already running...");
        }
        try {
            redisProcess = createRedisProcessBuilder().start();
            logErrors();
            awaitRedisServerReady();
            active = true;
        } catch (IOException e) {
            throw new EmbeddedRedisException("Failed to start Reddis instance", e);
        }
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
    public synchronized void stop() throws EmbeddedRedisException {
        if (active) {
            redisProcess.destroy();
            tryWaitFor();
            active = false;
        }
    }

    private void tryWaitFor() {
        try {
            redisProcess.waitFor();
        } catch (InterruptedException e) {
            throw new EmbeddedRedisException("Failed to stop redis instance", e);
        }
    }

    @Override
    public List<Integer> ports() {
        return Arrays.asList(port);
    }
}
