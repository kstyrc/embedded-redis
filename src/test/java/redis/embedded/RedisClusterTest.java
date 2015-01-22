package redis.embedded;

import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class RedisClusterTest {
    private Redis sentinel1;
    private Redis sentinel2;
    private Redis master1;
    private Redis master2;

    private RedisCluster instance;

    @Before
    public void setUp() throws Exception {
        sentinel1 = mock(Redis.class);
        sentinel2 = mock(Redis.class);
        master1 = mock(Redis.class);
        master2 = mock(Redis.class);
    }


    @Test
    public void stopShouldStopEntireCluster() throws Exception {
        //given
        final List<Redis> sentinels = Arrays.asList(sentinel1, sentinel2);
        final List<Redis> servers = Arrays.asList(master1, master2);
        instance = new RedisCluster(sentinels, servers);

        //when
        instance.stop();

        //then
        sentinels.stream().forEach(s -> verify(s).stop());
        servers.stream().forEach(m -> verify(m).stop());
    }

    @Test
    public void startShouldStartEntireCluster() throws Exception {
        //given
        final List<Redis> sentinels = Arrays.asList(sentinel1, sentinel2);
        final List<Redis> servers = Arrays.asList(master1, master2);
        instance = new RedisCluster(sentinels, servers);

        //when
        instance.start();

        //then
        sentinels.stream().forEach(s -> verify(s).start());
        servers.stream().forEach(m -> verify(m).start());
    }

    @Test
    public void isActiveShouldCheckEntireClusterIfAllActive() throws Exception {
        //given
        given(sentinel1.isActive()).willReturn(true);
        given(sentinel2.isActive()).willReturn(true);
        given(master1.isActive()).willReturn(true);
        given(master2.isActive()).willReturn(true);
        final List<Redis> sentinels = Arrays.asList(sentinel1, sentinel2);
        final List<Redis> servers = Arrays.asList(master1, master2);
        instance = new RedisCluster(sentinels, servers);

        //when
        instance.isActive();

        //then
        sentinels.stream().forEach(s -> verify(s).isActive());
        servers.stream().forEach(m -> verify(m).isActive());
    }
}