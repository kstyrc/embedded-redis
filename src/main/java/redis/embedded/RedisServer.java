package redis.embedded;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

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
	
	private final String command;
	private final Integer port;
	
	private boolean active = false;
	private Process redisProcess;
	
	public RedisServer(String commandFullPath, Integer port) {
		this.command = commandFullPath;
		this.port = port;
	}
	
	public RedisServer(Integer port) {
		this(Resources.getResource(RedisRunScriptEnum.getRedisRunScript()).getPath(), port);
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
		String outputLine = null;
		do {
			outputLine = reader.readLine();
		} while (outputLine != null && !outputLine.matches(REDIS_READY_PATTERN));
	}

	private ProcessBuilder getRedisProcessBuilder() {
		ProcessBuilder pb;
		pb = new ProcessBuilder(command, "--port", Integer.toString(port));
		File redisTmpDir = Files.createTempDir();
		redisTmpDir.deleteOnExit();
		pb.directory(redisTmpDir);
		
		File redisServer = new File(command);
		redisServer.setExecutable(true);
		return pb;
	}
	
	public synchronized void stop() {
		if (active) {
			redisProcess.destroy();
			active = false;
		}
	}
}

