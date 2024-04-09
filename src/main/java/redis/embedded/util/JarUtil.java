package redis.embedded.util;

import com.google.common.io.Files;
import com.google.common.io.Resources;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

public class JarUtil {

    public static File extractExecutableFromJar(String executable) throws IOException {
        // To begin with, you need to check if the temporary directory exists. If it doesn't exist, create the temporary directory first.
        File baseDir = new File(System.getProperty("java.io.tmpdir"));
        if (!baseDir.exists()) {
            baseDir.mkdirs();
        }
        File tmpDir = Files.createTempDir();
        tmpDir.deleteOnExit();

        File command = new File(tmpDir, executable);
        FileUtils.copyURLToFile(Resources.getResource(executable), command);
        command.deleteOnExit();
        command.setExecutable(true);

        return command;
    }
}
