package redis.embedded.ports;

import org.junit.Test;
import redis.embedded.exceptions.RedisBuildingException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class PredefinedPortProviderTest {

    @Test
    public void nextShouldGiveNextPortFromAssignedList() throws Exception {
        //given
        Collection<Integer> ports = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
        final PredefinedPortProvider provider = new PredefinedPortProvider(ports);

        //when
        final List<Integer> returnedPorts = new ArrayList<Integer>();
        for(int i = 0;i < ports.size(); i++) {
            returnedPorts.add(provider.next());
        }

        //then
        assertEquals(ports, returnedPorts);
    }

    @Test(expected = RedisBuildingException.class)
    public void nextShouldThrowExceptionWhenRunOutsOfPorts() throws Exception {
        //given
        Collection<Integer> ports = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
        final PredefinedPortProvider provider = new PredefinedPortProvider(ports);

        //when
        for(int i = 0;i < ports.size(); i++) {
            provider.next();
        }

        //then exception should be thrown...
        provider.next();
    }
}