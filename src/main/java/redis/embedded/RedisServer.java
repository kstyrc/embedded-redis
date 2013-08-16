package redis.embedded;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

import com.google.common.io.ByteStreams;
import com.google.common.io.Files;
import com.google.common.io.Resources;

public class RedisServer {
	
	private static enum RedisRunScriptEnum {
		WINDOWS_32("redis-server.exe"),
		WINDOWS_64("redis-server-64.exe"),
		UNIX("redis-server");
		
		private final String runScript;

		private RedisRunScriptEnum(String runScript) {
			this.runScript = runScript;
		}
		
		public static String getRedisRunScript() {
			String osName = System.getProperty("os.name");
			String osArch = System.getProperty("os.arch");
			
			if (osName.indexOf("win") >= 0) {
				if (osArch.indexOf("64") >= 0) {
					return WINDOWS_64.runScript;
				} else {
					return WINDOWS_32.runScript;
				}
			} else if (osName.indexOf("nix") >= 0 || osName.indexOf("nux") >= 0 || osName.indexOf("aix") > 0) {
				return UNIX.runScript;
			} else {
				throw new RuntimeException("Unsupported os...");
			}
		}
	}
	
	private static final String REDIS_READY_PATTERN = ".*The server is now ready to accept connections on port.*";

	private final File command;
	private final Integer port;
	private final File dir;

	private boolean active = false;
	private Process redisProcess;

	public RedisServer(File command, Integer port) {
		this.command = command;
		this.port = port;
		this.dir = command.getParentFile();
	}

	public RedisServer(Integer port) throws IOException {
		String redisRunScript = RedisRunScriptEnum.getRedisRunScript();

		this.dir = Files.createTempDir();
		this.dir.deleteOnExit();

		this.command = new File(this.dir, redisRunScript);

		BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(this.command));
		try {
			URL url = Resources.getResource(redisRunScript);
			BufferedInputStream bufferedInputStream = new BufferedInputStream(url.openStream());
			try {
				ByteStreams.copy(bufferedInputStream, bufferedOutputStream);
			} finally {
				bufferedInputStream.close();
			}
		} finally {
			bufferedOutputStream.close();
		}
		this.command.setExecutable(true);
		this.command.deleteOnExit();

		this.port = port;
	}

	public synchronized void start() throws IOException {
		if (active) {
			throw new RuntimeException("This redis server instance is already running...");
		}

		redisProcess = getRedisProcessBuilder().start();
		active = true;

		awaitRedisServerReady();
	}

	private void awaitRedisServerReady() throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(redisProcess.getInputStream()));
		try {
			String outputLine = null;
			do {
				outputLine = reader.readLine();
			} while (outputLine != null && !outputLine.matches(REDIS_READY_PATTERN));
		} finally {
			reader.close();
		}
	}

	private ProcessBuilder getRedisProcessBuilder() {
		ProcessBuilder pb = new ProcessBuilder(command.getAbsolutePath(), "--port", Integer.toString(port));
		pb.directory(this.dir);

		return pb;
	}

	public synchronized void stop() {
		if (active) {
			redisProcess.destroy();
			active = false;
		}
	}
}