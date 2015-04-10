package redis.embedded.ports;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class SequencePortProviderTest {

    @Test
    public void nextShouldIncrementPorts() throws Exception {
        //given
        final int startPort = 10;
        final int portCount = 101;
        final SequencePortProvider provider = new SequencePortProvider(startPort);

        //when
        int max = 0;
        for(int i = 0;i<portCount; i++) {
            int port = provider.next();
            if(port > max) {
                max = port;
            }
        }

        //then
        assertEquals(portCount + startPort - 1, max);
    }
}