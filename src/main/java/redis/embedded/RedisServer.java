package redis.embedded;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.commons.io.FileUtils;

import com.google.common.io.Files;
import com.google.common.io.Resources;

public class RedisServer {
	
	private static enum RedisRunScriptEnum {
		WINDOWS_32("redis-server.exe"),
		WINDOWS_64("redis-server-64.exe"),
		UNIX("redis-server"),
		MACOSX("redis-server.app");
		
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
			} else if ("Mac OS X".equals(osName)) {
				return MACOSX.runScript;
			} else {
				throw new RuntimeException("Unsupported os/architecture...: " + osName + " on " + osArch);
			}
		}
	}
	
	private static final String REDIS_READY_PATTERN = ".*The server is now ready to accept connections on port.*";

	private final File command;
	private final Integer port;

	private volatile boolean active = false;
	private Process redisProcess;

	public RedisServer(File command, Integer port) {
		this.command = command;
		this.port = port;
	}

	public RedisServer(Integer port) throws IOException {
		this.port = port;
		this.command = extractExecutableFromJar(RedisRunScriptEnum.getRedisRunScript());
	}

	private File extractExecutableFromJar(String scriptName) throws IOException {
		File tmpDir = Files.createTempDir();
		tmpDir.deleteOnExit();

		File command = new File(tmpDir, scriptName);
		FileUtils.copyURLToFile(Resources.getResource(scriptName), command);
		command.deleteOnExit();
		command.setExecutable(true);
		
		return command;
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
			} while (outputLine != null && !outputLine.matches(REDIS_READY_PATTERN));
		} finally {
			reader.close();
		}
	}

	private ProcessBuilder createRedisProcessBuilder() {
		ProcessBuilder pb = new ProcessBuilder(command.getAbsolutePath(), "--port", Integer.toString(port));
		pb.directory(command.getParentFile());

		return pb;
	}

	public synchronized void stop() {
		if (active) {
			redisProcess.destroy();
			active = false;
		}
	}
}
