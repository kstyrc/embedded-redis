package redis.embedded;

import com.google.common.base.Preconditions;
import com.google.common.io.Files;
import redis.embedded.exceptions.RedisBuildingException;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class RedisSentinelBuilder {
    private static final String LINE_SEPARATOR = System.getProperty("line.separator");
    private static final String CONF_FILENAME = "embedded-redis-sentinel";
    private static final String MASTER_MONITOR_LINE = "sentinel monitor %s 127.0.0.1 %d %d";
    private static final String DOWN_AFTER_LINE = "sentinel down-after-milliseconds %s %d";
    private static final String FAILOVER_LINE = "sentinel failover-timeout %s %d";
    private static final String PARALLEL_SYNCS_LINE = "sentinel parallel-syncs %s %d";
    private static final String PORT_LINE = "port %d";

    private File executable;
    private RedisExecProvider redisExecProvider = RedisExecProvider.defaultProvider();
    private Integer port = 26379;
    private int masterPort = 6379;
    private String masterName = "mymaster";
    private long downAfterMilliseconds = 60000L;
    private long failoverTimeout = 180000L;
    private int parallelSyncs = 1;
    private int quorumSize = 1;
    private String sentinelConf;

    private StringBuilder redisConfigBuilder;

    public RedisSentinelBuilder redisExecProvider(RedisExecProvider redisExecProvider) {
        this.redisExecProvider = redisExecProvider;
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

    public RedisSentinelBuilder quorumSize(int quorumSize) {
        this.quorumSize = quorumSize;
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
            throw new RedisBuildingException("Redis configuration is already partially build using setting(String) method!");
        }
        this.sentinelConf = redisConf;
        return this;
    }

    public RedisSentinelBuilder setting(String configLine) {
        if (sentinelConf != null) {
            throw new RedisBuildingException("Redis configuration is already set using redis conf file!");
        }

        if (redisConfigBuilder == null) {
            redisConfigBuilder = new StringBuilder();
        }

        redisConfigBuilder.append(configLine);
        redisConfigBuilder.append(LINE_SEPARATOR);
        return this;
    }

    public RedisSentinel build() {
        tryResolveConfAndExec();
        List<String> args = buildCommandArgs();
        return new RedisSentinel(args, port);
    }

    private void tryResolveConfAndExec() {
        try {
            if (sentinelConf == null) {
                resolveSentinelConf();
            }
            executable = redisExecProvider.get();
        } catch (Exception e) {
            throw new RedisBuildingException("Could not build sentinel instance", e);
        }
    }

    public void reset() {
        this.redisConfigBuilder = null;
        this.sentinelConf = null;
    }

    public void addDefaultReplicationGroup() {
        setting(String.format(MASTER_MONITOR_LINE, masterName, masterPort, quorumSize));
        setting(String.format(DOWN_AFTER_LINE, masterName, downAfterMilliseconds));
        setting(String.format(FAILOVER_LINE, masterName, failoverTimeout));
        setting(String.format(PARALLEL_SYNCS_LINE, masterName, parallelSyncs));
    }

    private void resolveSentinelConf() throws IOException {
        if (redisConfigBuilder == null) {
            addDefaultReplicationGroup();
        }
        setting(String.format(PORT_LINE, port));
        final String configString = redisConfigBuilder.toString();

        File redisConfigFile = File.createTempFile(resolveConfigName(), ".conf");
        redisConfigFile.deleteOnExit();
        Files.write(configString, redisConfigFile, Charset.forName("UTF-8"));
        sentinelConf = redisConfigFile.getAbsolutePath();
    }

    private String resolveConfigName() {
        return CONF_FILENAME + "_" + port;
    }

    private List<String> buildCommandArgs() {
        Preconditions.checkNotNull(sentinelConf);

        List<String> args = new ArrayList<String>();
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
