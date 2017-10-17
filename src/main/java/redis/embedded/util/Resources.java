package redis.embedded.util;

import java.net.URL;

public class Resources {
    public static URL getResource(String resource) {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        if (cl == null) {
            cl = Resources.class.getClassLoader();
        }

        URL url = cl.getResource(resource);

        if (url == null) {
            throw new IllegalArgumentException("resource " + resource + " not found");
        }

        return url;
    }
}
