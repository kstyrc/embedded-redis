package redis.embedded;

import com.google.common.base.Preconditions;
import com.google.common.io.Files;
import redis.embedded.util.JarUtil;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by piotrturek on 22/01/15.
 */
public class RedisSentinelBuilder {
    private static final String LINE_SEPARATOR = System.getProperty("line.separator");
    private static final String CONF_FILENAME = "embedded-redis-sentinel";
    private static final String MASTER_MONITOR_LINE = "sentinel monitor %s 127.0.0.1 %d 1";
    private static final String DOWN_AFTER_LINE = "sentinel down-after-milliseconds %s %d";
    private static final String FAILOVER_LINE = "sentinel failover-timeout %s %d";
    private static final String PARALLEL_SYNCS_LINE = "sentinel parallel-syncs %s %d";

    private File executable;
    private Integer port;
    private int masterPort = 6379;
    private String masterName = "mymaster";
    private long downAfterMilliseconds = 60000L;
    private long failoverTimeout = 180000L;
    private int parallelSyncs = 1;
    private String sentinelConf;

    private StringBuilder redisConfigBuilder;

    public RedisSentinelBuilder executable(File executable) {
        this.executable = executable;
        return this;
    }

    public RedisSentinelBuilder executable(String executable) {
        this.executable = new File(executable);
        return this;
    }

    public RedisSentinelBuilder port(Integer port) {
        this.port = port;
        return this;
    }

    public RedisSentinelBuilder masterPort(Integer masterPort) {
        this.masterPort = masterPort;
        return this;
    }

    public RedisSentinelBuilder masterName(String masterName) {
        this.masterName = masterName;
        return this;
    }

    public RedisSentinelBuilder downAfterMilliseconds(Long downAfterMilliseconds) {
        this.downAfterMilliseconds = downAfterMilliseconds;
        return this;
    }

    public RedisSentinelBuilder failoverTimeout(Long failoverTimeout) {
        this.failoverTimeout = failoverTimeout;
        return this;
    }

    public RedisSentinelBuilder parallelSyncs(int parallelSyncs) {
        this.parallelSyncs = parallelSyncs;
        return this;
    }

    public RedisSentinelBuilder configFile(String redisConf) {
        if (redisConfigBuilder != null) {
            throw new RuntimeException("Redis configuration is already partially build using setting(String) method!");
        }
        this.sentinelConf = redisConf;
        return this;
    }

    public RedisSentinelBuilder setting(String configLine) {
        if (sentinelConf != null) {
            throw new RuntimeException("Redis configuration is already set using redis conf file!");
        }

        if (redisConfigBuilder == null) {
            redisConfigBuilder = new StringBuilder();
        }

        redisConfigBuilder.append(configLine);
        redisConfigBuilder.append(LINE_SEPARATOR);
        return this;
    }

    public RedisSentinel build() throws IOException {
        if (sentinelConf == null) {
            resolveSentinelConf();
        }
        if (executable == null) {
            executable = JarUtil.extractExecutableFromJar(RedisRunScriptEnum.getRedisRunScript());
        }

        List<String> args = buildCommandArgs();
        return new RedisSentinel(args);
    }

    private void resolveSentinelConf() throws IOException {
        if (redisConfigBuilder == null) {
            useDefaultConfig();
        }
        final String configString = redisConfigBuilder.toString();

        File redisConfigFile = File.createTempFile(CONF_FILENAME, ".conf");
        redisConfigFile.deleteOnExit();
        Files.write(configString, redisConfigFile, Charset.forName("UTF-8"));
        sentinelConf = redisConfigFile.getAbsolutePath();
    }

    private void useDefaultConfig() {
        setting(String.format(MASTER_MONITOR_LINE, masterName, masterPort));
        setting(String.format(DOWN_AFTER_LINE, masterName, downAfterMilliseconds));
        setting(String.format(FAILOVER_LINE, masterName, failoverTimeout));
        setting(String.format(PARALLEL_SYNCS_LINE, masterName, parallelSyncs));
    }

    private List<String> buildCommandArgs() {
        Preconditions.checkNotNull(sentinelConf);

        List<String> args = new ArrayList<>();
        args.add(executable.getAbsolutePath());
        args.add(sentinelConf);
        args.add("--sentinel");

        if (port != null) {
            args.add("--port");
            args.add(Integer.toString(port));
        }

        return args;
    }
}
