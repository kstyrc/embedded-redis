package redis.embedded.ports;

import org.junit.Test;
import redis.embedded.exceptions.RedisBuildingException;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;

public class EphemeralPortProviderTest {
    private final ExecutorService executor = Executors.newFixedThreadPool(10);

    @Test
    public void nextShouldGiveNextFreeEphemeralPortConcurrently() throws Exception {
        //given
        final EphemeralPortProvider provider = new EphemeralPortProvider();

        //when
        final List<Integer> ports = executor.invokeAll(
                Stream.generate(() -> (Callable<Integer>) provider::next).limit(20).collect(Collectors.toList()))
                .stream()
                .map(this::getResult)
                .sorted()
                .distinct()
                .collect(Collectors.toList());

        //then
        System.out.println(ports);
        assertEquals(20, ports.size());
    }

    private int getResult(Future<Integer> f) {
        int ret = 0;
        try {
            ret = f.get();
        } catch (Exception e) {
            throw new RedisBuildingException("failed to deliver", e);
        }
        return ret;
    }

}