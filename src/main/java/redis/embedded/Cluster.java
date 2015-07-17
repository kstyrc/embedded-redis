package redis.embedded;

/**
 * Created by dragan on 17.07.15.
 */
public interface Cluster {
    void create();
    void start() throws InterruptedException;

    boolean isActive();

    void stop();
}
