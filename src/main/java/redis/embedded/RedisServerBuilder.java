package redis.embedded;

import com.google.common.base.Strings;
import com.google.common.io.Files;
import redis.embedded.exceptions.RedisBuildingException;
import redis.embedded.util.JarUtil;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by piotrturek on 22/01/15.
 */
public class RedisServerBuilder {
    private static final String LINE_SEPARATOR = System.getProperty("line.separator");
    private static final String CONF_FILENAME = "embedded-redis-server";

    private File executable;
    private Integer port = 6379;
    private InetSocketAddress slaveOf;
    private String redisConf;

    private StringBuilder redisConfigBuilder;

    public RedisServerBuilder executable(File executable) {
        this.executable = executable;
        return this;
    }

    public RedisServerBuilder executable(String executable) {
        this.executable = new File(executable);
        return this;
    }

    public RedisServerBuilder port(Integer port) {
        this.port = port;
        return this;
    }

    public RedisServerBuilder slaveOf(String hostname, Integer port) {
        this.slaveOf = new InetSocketAddress(hostname, port);
        return this;
    }

    public RedisServerBuilder slaveOf(InetSocketAddress slaveOf) {
        this.slaveOf = slaveOf;
        return this;
    }

    public RedisServerBuilder configFile(String redisConf) {
        if (redisConfigBuilder != null) {
            throw new RedisBuildingException("Redis configuration is already partially build using setting(String) method!");
        }
        this.redisConf = redisConf;
        return this;
    }

    public RedisServerBuilder setting(String configLine) {
        if (redisConf != null) {
            throw new RedisBuildingException("Redis configuration is already set using redis conf file!");
        }

        if (redisConfigBuilder == null) {
            redisConfigBuilder = new StringBuilder();
        }

        redisConfigBuilder.append(configLine);
        redisConfigBuilder.append(LINE_SEPARATOR);
        return this;
    }

    public RedisServer build() {
        tryResolveConfAndExec();
        List<String> args = buildCommandArgs();
        return new RedisServer(args, port);
    }

    public void reset() {
        this.redisConfigBuilder = null;
        this.slaveOf = null;
        this.redisConf = null;
    }

    private void tryResolveConfAndExec() {
        try {
            resolveConfAndExec();
        } catch (IOException e) {
            throw new RedisBuildingException("Could not build server instance", e);
        }
    }

    private void resolveConfAndExec() throws IOException {
        if (redisConf == null && redisConfigBuilder != null) {
            File redisConfigFile = File.createTempFile(resolveConfigName(), ".conf");
            redisConfigFile.deleteOnExit();
            Files.write(redisConfigBuilder.toString(), redisConfigFile, Charset.forName("UTF-8"));
            redisConf = redisConfigFile.getAbsolutePath();
        }

        if (executable == null) {
            executable = JarUtil.extractExecutableFromJar(RedisRunScriptEnum.getRedisRunScript());
        }
    }

    private String resolveConfigName() {
        return CONF_FILENAME + "_" + port;
    }

    private List<String> buildCommandArgs() {
        List<String> args = new ArrayList<>();
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

        return args;
    }
}
