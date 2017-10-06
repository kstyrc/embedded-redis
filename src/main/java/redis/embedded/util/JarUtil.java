package redis.embedded.util;

import java.io.File;
import java.io.IOException;

public class JarUtil {

    public static File extractExecutableFromJar(String executable) throws IOException {
        File tmpDir = Files.createTempDir();
        tmpDir.deleteOnExit();

        File command = new File(tmpDir, executable);

        Files.copyURLToFile(Resources.getResource(executable), command);
        command.deleteOnExit();
        command.setExecutable(true);

        return command;
    }
}
