package redis.embedded.ports;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class EphemeralPortProviderTest {

    @Test
    public void nextShouldGiveNextFreeEphemeralPort() throws Exception {
        //given
        final int portCount = 20;
        final EphemeralPortProvider provider = new EphemeralPortProvider();

        //when
        final List<Integer> ports = new ArrayList<Integer>();
        for(int i = 0;i < portCount; i++) {
            ports.add(provider.next());
        }

        //then
        System.out.println(ports);
        assertEquals(20, ports.size());
    }
}