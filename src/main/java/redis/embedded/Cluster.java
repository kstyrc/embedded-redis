package redis.embedded;

/**
 * Created by dragan on 17.07.15.
 */
public interface Cluster extends Redis{
    void create();
    void start();

    boolean isActive();

    void stop();
}
