package redis.embedded;

import redis.embedded.util.JarUtil;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RedisServer extends AbstractRedisInstance {
    RedisServer(Integer port) throws IOException {
        File executable = JarUtil.extractExecutableFromJar(RedisRunScriptEnum.getRedisRunScript());
        this.args = Arrays.asList(
                executable.getAbsolutePath(),
                "--port", Integer.toString(port)
        );
	}

    RedisServer(File executable, Integer port) {
        this.args = Arrays.asList(
                executable.getAbsolutePath(),
                "--port", Integer.toString(port)
        );
    }

    RedisServer(List<String> args) {
        this.args = new ArrayList<>(args);
    }

}
