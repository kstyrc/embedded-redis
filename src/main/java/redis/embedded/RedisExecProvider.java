package redis.embedded;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import redis.embedded.util.Architecture;
import redis.embedded.util.JarUtil;
import redis.embedded.util.OS;
import redis.embedded.util.OsArchitecture;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import static java.util.Objects.requireNonNull;
import static redis.embedded.util.OsArchitecture.MAC_OS_X_x86;
import static redis.embedded.util.OsArchitecture.MAC_OS_X_x86_64;
import static redis.embedded.util.OsArchitecture.UNIX_x86;
import static redis.embedded.util.OsArchitecture.UNIX_x86_64;
import static redis.embedded.util.OsArchitecture.WINDOWS_x86;
import static redis.embedded.util.OsArchitecture.WINDOWS_x86_64;

/**
 * Default Redis version: 2.8
 * <br>
 * Available Redis versions:
 * <ul>
 * <li>2.8 - {@code WINDOWS_x86}, {@code WINDOWS_x86_64}, {@code UNIX_x86}, {@code UNIX_x86_64}, {@code MAC_OS_X_x86}, {@code MAC_OS_X_x86_64}</li>
 * <li>3.0 - {@code UNIX_x86_64}</li>
 * <li>3.2 - {@code UNIX_x86_64}</li>
 * <li>4.0 - {@code UNIX_x86_64}</li>
 * <li>5.0 - {@code UNIX_x86_64}</li>
 * </ul>
 */
public class RedisExecProvider {

    private static final String DEFAULT_VERSION = "2.8";

    private final Map<ExecutableKey, String> executables = Maps.newHashMap();
    private final String redisVersion;

    public static RedisExecProvider defaultProvider() {
        return defaultProvider(DEFAULT_VERSION);
    }
    
    public static RedisExecProvider defaultProvider(String redisVersion) {
        return new RedisExecProvider(redisVersion);
    }

    private RedisExecProvider(String redisVersion) {
        this.redisVersion = redisVersion;
        initExecutables();
    }

    private void initExecutables() {
        executables.put(ExecutableKey.of(WINDOWS_x86, "2.8"), "redis-server-2.8.19.exe");
        executables.put(ExecutableKey.of(WINDOWS_x86_64, "2.8"), "redis-server-2.8.19.exe");

        executables.put(ExecutableKey.of(UNIX_x86, "2.8"), "redis-server-2.8.19-32");
        executables.put(ExecutableKey.of(UNIX_x86_64, "2.8"), "redis-server-2.8.19");

        executables.put(ExecutableKey.of(MAC_OS_X_x86, "2.8"), "redis-server-2.8.19.app");
        executables.put(ExecutableKey.of(MAC_OS_X_x86_64, "2.8"), "redis-server-2.8.19.app");

        executables.put(ExecutableKey.of(UNIX_x86_64, "3.0"), "redis-server-3.0.7");
        executables.put(ExecutableKey.of(UNIX_x86_64, "3.2"), "redis-server-3.2.13");
        executables.put(ExecutableKey.of(UNIX_x86_64, "4.0"), "redis-server-4.0.14");
        executables.put(ExecutableKey.of(UNIX_x86_64, "5.0"), "redis-server-5.0.5");
    }

    public RedisExecProvider override(OS os, String executable) {
        Preconditions.checkNotNull(executable);
        for (Architecture arch : Architecture.values()) {
            override(os, arch, executable);
        }
        return this;
    }

    public RedisExecProvider override(OS os, Architecture arch, String executable) {
        Preconditions.checkNotNull(executable);
        executables.put(ExecutableKey.of(new OsArchitecture(os, arch), redisVersion), executable);
        return this;
    }

    public File get() throws IOException {
        OsArchitecture osArch = OsArchitecture.detect();
        String executablePath = executables.get(ExecutableKey.of(osArch, redisVersion));
        return fileExists(executablePath) ?
            new File(executablePath) :
            JarUtil.extractExecutableFromJar(executablePath);
    }

    private boolean fileExists(String executablePath) {
        return new File(executablePath).exists();
    }

    private static class ExecutableKey {
        private OsArchitecture architecture;
        private String version;

        private static ExecutableKey of(OsArchitecture architecture, String version) {
            return new ExecutableKey(architecture, version);
        }

        private ExecutableKey(OsArchitecture architecture, String version) {
            this.architecture = requireNonNull(architecture);
            this.version = requireNonNull(version);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            ExecutableKey that = (ExecutableKey) o;

            if (!architecture.equals(that.architecture)) return false;
            return version.equals(that.version);

        }

        @Override
        public int hashCode() {
            int result = architecture.hashCode();
            result = 31 * result + version.hashCode();
            return result;
        }
    }
}
