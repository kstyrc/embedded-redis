package redis.embedded.util;

import java.io.*;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.UUID;

public class Files {
    public static void write(String from, File to, Charset charset) throws IOException {
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(to);
            OutputStreamWriter osw = new OutputStreamWriter(fos, charset);
            osw.write(from);
            osw.flush();
            osw.close();
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (Throwable ignored) {
                    //ignore
                }
            }
        }
    }

    public static File createTempDir() throws IOException {
        File baseDir = new File(System.getProperty("java.io.tmpdir"));
        File tmpDir = new File(baseDir, UUID.randomUUID().toString());
        if (tmpDir.mkdir()) {
            return tmpDir;
        }
        throw new IllegalStateException("Failed to create temp directory " + tmpDir.getAbsolutePath());
    }

    public static void copyURLToFile(URL url, File destination) throws IOException {
        FileOutputStream dest = null;
        InputStream src = null;
        byte[] buf = new byte[16 * 1024];
        int bytesRead;

        try {
            src = url.openStream();
            dest = new FileOutputStream(destination);
            while ((bytesRead = src.read(buf)) != -1) {
                dest.write(buf, 0, bytesRead);
            }
            dest.flush();
            dest.close();
            src.close();
        } finally {
            if (dest != null) {
                try {
                    dest.close();
                } catch (Throwable ignored) {
                    //ignore
                }
            }
            if (src != null) {
                try {
                    src.close();
                } catch (Throwable ignored) {
                    //ignore
                }
            }
        }
    }
}
