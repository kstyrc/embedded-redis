package redis.embedded.ports;

import org.junit.Test;
import redis.embedded.exceptions.RedisBuildingException;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;

public class PredefinedPortProviderTest {
    private final ExecutorService executor = Executors.newFixedThreadPool(10);

    @Test
    public void nextShouldGiveNextPortFromAssignedListConcurrently() throws Exception {
        //given
        Collection<Integer> ports = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
        final PredefinedPortProvider provider = new PredefinedPortProvider(ports);

        //when
        final List<Integer> returnedPorts = executor.invokeAll(
                Stream.generate(() -> (Callable<Integer>) provider::next).limit(10).collect(Collectors.toList()))
                .stream()
                .map(this::getResult)
                .sorted()
                .distinct()
                .collect(Collectors.toList());

        //then
        assertEquals(ports, returnedPorts);
    }

    @Test(expected = RedisBuildingException.class)
    public void nextShouldThrowExceptionWhenRunOutsOfPorts() throws Exception {
        //given
        Collection<Integer> ports = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
        final PredefinedPortProvider provider = new PredefinedPortProvider(ports);

        //when
        executor.invokeAll(
                Stream.generate(() -> (Callable<Integer>) provider::next).limit(11).collect(Collectors.toList()))
                .stream()
                .map(this::getResult)
                .sorted()
                .distinct()
                .collect(Collectors.toList());

        //then exception should be thrown...
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