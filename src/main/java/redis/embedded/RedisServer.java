package redis.embedded;

import com.google.common.base.Strings;
import com.google.common.io.Files;
import redis.embedded.util.JarUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
			String osName = System.getProperty("os.name").toLowerCase();
			String osArch = System.getProperty("os.arch").toLowerCase();

			if (osName.contains("win")) {
				if (osArch.contains("64")) {
					return WINDOWS_64.runScript;
				} else {
					return WINDOWS_32.runScript;
				}
			} else if (osName.contains("nix") || osName.contains("nux") || osName.contains("aix")) {
				return UNIX.runScript;
			} else if ("Mac OS X".equalsIgnoreCase(osName)) {
				return MACOSX.runScript;
			} else {
				throw new RuntimeException("Unsupported os/architecture...: " + osName + " on " + osArch);
			}
		}
	}

    public static class Builder {

        private static final String LINE_SEPARATOR = System.getProperty("line.separator");

        private File executable;
        private Integer port;
        private InetSocketAddress slaveOf;
        private String redisConf;
        private boolean sentinel = false;

        private StringBuilder redisConfigBuilder;

        public Builder executable(File executable) {
            this.executable = executable;
            return this;
        }

        public Builder executable(String executable) {
            this.executable = new File(executable);
            return this;
        }

        public Builder port(Integer port) {
            this.port = port;
            return this;
        }

        public Builder slaveOf(String hostname, Integer port) {
            this.slaveOf = new InetSocketAddress(hostname, port);
            return this;
        }

        public Builder slaveOf(InetSocketAddress slaveOf) {
            this.slaveOf = slaveOf;
            return this;
        }
        
        public Builder sentinel() {
            this.sentinel = true;
            if (port == null) {
                port = 26379;
            }
            return this;
        }

        public Builder configFile(String redisConf) {
            if (redisConfigBuilder != null) {
                throw new RuntimeException("Redis configuration is already partially build using setting(String) method!");
            }
            this.redisConf = redisConf;
            return this;
        }

        public Builder setting(String configLine) {
            if (redisConf != null) {
                throw new RuntimeException("Redis configuration is already set using redis conf file!");
            }

            if (redisConfigBuilder == null) {
                redisConfigBuilder = new StringBuilder();
            }

            redisConfigBuilder.append(configLine);
            redisConfigBuilder.append(LINE_SEPARATOR);
            return this;
        }

        public RedisServer build() throws IOException {
            
            if (sentinel && redisConf == null && redisConfigBuilder == null) {
                redisConfigBuilder = new StringBuilder();
            }

            if (redisConf == null && redisConfigBuilder != null) {
                File redisConfigFile = File.createTempFile("embedded-redis", ".conf");
                redisConfigFile.deleteOnExit();
                Files.write(redisConfigBuilder.toString(), redisConfigFile, Charset.forName("UTF-8"));
                redisConf = redisConfigFile.getAbsolutePath();
            }

            if (executable == null) {
                executable = JarUtil.extractExecutableFromJar(RedisRunScriptEnum.getRedisRunScript());
            }

            List<String> args = buildCommandArgs();
            return new RedisServer(args);
        }

        private List<String> buildCommandArgs() {
            List<String> args = new ArrayList<String>();
            args.add(executable.getAbsolutePath());

            if (!Strings.isNullOrEmpty(redisConf)) {
                args.add(redisConf);
            }

            if (port != null) {
                args.add("--port");
                args.add(Integer.toString(port));
            }

            if (slaveOf != null) {
                args.add("--slaveof");
                args.add(slaveOf.getHostName());
                args.add(Integer.toString(slaveOf.getPort()));
            }
            
            if (sentinel) {
                args.add("--sentinel");
            }

            return args;
        }
    }

	private static final String REDIS_READY_PATTERN = ".*The server is now ready to accept connections on port.*";
        private static final String SENTINEL_READY_PATTERN = ".*Running in sentinel mode.*";
        private static final int DEFAULT_PORT = 6379;
        private static final int DEFAULT_SENTINEL_PORT = 26379;

        private final List<String> args;

	private volatile boolean active = false;
	private Process redisProcess;
        private String host = "127.0.0.1";
        private final int port;
        private boolean sentinel;

	public RedisServer(Integer port) throws IOException {
        File executable = JarUtil.extractExecutableFromJar(RedisRunScriptEnum.getRedisRunScript());
        this.port = port;
        this.args = Arrays.asList(
                executable.getAbsolutePath(),
                "--port", Integer.toString(port)
        );
	}

    public RedisServer(File executable, Integer port) {
        this.port = port;
        this.args = Arrays.asList(
                executable.getAbsolutePath(),
                "--port", Integer.toString(port)
        );
    }

    private RedisServer(List<String> args) {
        sentinel = args.indexOf("--sentinel") != -1;
        int ihost = args.indexOf("--host");
        if (ihost != -1) {
            this.host = args.get(ihost + 1);
        }
        int iport = args.indexOf("--port");
        if (iport != -1) {
            this.port = Integer.parseInt(args.get(iport + 1));
        } else if (sentinel) {
            this.port = DEFAULT_SENTINEL_PORT;
        } else {
            this.port = DEFAULT_PORT;
        }
        this.args = new ArrayList<String>(args);
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getHost() {
        return host;
    }
    
    public int getPort() {
        return port;
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
			String pattern = sentinel ? SENTINEL_READY_PATTERN : REDIS_READY_PATTERN;
                        do {
                            outputLine = reader.readLine();

                if (outputLine == null) {
                    //Something goes wrong. Stream is ended before server was activated.
                    throw new RuntimeException("Can't start redis server. Check logs for details.");
                }
            } while (!outputLine.matches(pattern));
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
