package redis.embedded.ports;

import org.junit.Test;

import java.util.Comparator;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;

public class SequencePortProviderTest {
    private final ExecutorService executor = Executors.newFixedThreadPool(10);

    @Test
    public void nextShouldIncrementPortsConcurrently() throws Exception {
        //given
        final int startPort = 10;
        final SequencePortProvider provider = new SequencePortProvider(startPort);

        //when
        final int max = executor.invokeAll(
                Stream.generate(() -> (Callable<Integer>) provider::next).limit(101).collect(Collectors.toList()))
                    .stream()
                    .map(this::getResult)
                    .max(Comparator.<Integer>naturalOrder())
                    .get();

        //then
        assertEquals(110, max);
    }

    private int getResult(Future<Integer> f) {
        int ret = 0;
        try {
            ret = f.get();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ret;
    }
}