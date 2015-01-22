package redis.embedded;

import redis.embedded.util.JarUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RedisServer {

    private static final String REDIS_READY_PATTERN = ".*The server is now ready to accept connections on port.*";

    private final List<String> args;

	private volatile boolean active = false;
	private Process redisProcess;

	public RedisServer(Integer port) throws IOException {
        File executable = JarUtil.extractExecutableFromJar(RedisRunScriptEnum.getRedisRunScript());
        this.args = Arrays.asList(
                executable.getAbsolutePath(),
                "--port", Integer.toString(port)
        );
	}

    public RedisServer(File executable, Integer port) {
        this.args = Arrays.asList(
                executable.getAbsolutePath(),
                "--port", Integer.toString(port)
        );
    }

    RedisServer(List<String> args) {
        this.args = new ArrayList<String>(args);
    }

    public static RedisServerBuilder builder() {
        return new RedisServerBuilder();
    }

	public boolean isActive() {
		return active;
	}

	public synchronized void start() throws IOException {
		if (active) {
			throw new RuntimeException("This redis server instance is already running...");
		}
		redisProcess = createRedisProcessBuilder().start();
		awaitRedisServerReady();
		active = true;
	}

	private void awaitRedisServerReady() throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(redisProcess.getInputStream()));
		try {
			String outputLine = null;
			do {
				outputLine = reader.readLine();

                if (outputLine == null) {
                    //Something goes wrong. Stream is ended before server was activated.
                    throw new RuntimeException("Can't start redis server. Check logs for details.");
                }
            } while (!outputLine.matches(REDIS_READY_PATTERN));
		} finally {
			reader.close();
		}
	}

	private ProcessBuilder createRedisProcessBuilder() {
        File executable = new File(args.get(0));
		ProcessBuilder pb = new ProcessBuilder(args);
		pb.directory(executable.getParentFile());
		pb.redirectErrorStream();
		return pb;
	}

    public synchronized void stop() throws InterruptedException {
		if (active) {
			redisProcess.destroy();
			redisProcess.waitFor();
			active = false;
		}
	}
}
