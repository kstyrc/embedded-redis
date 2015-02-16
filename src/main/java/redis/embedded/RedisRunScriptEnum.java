package redis.embedded;

/**
* Created by piotrturek on 22/01/15.
*/
enum RedisRunScriptEnum {
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
