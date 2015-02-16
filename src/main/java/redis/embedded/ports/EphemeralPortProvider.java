package redis.embedded.ports;

import redis.embedded.PortProvider;
import redis.embedded.exceptions.RedisBuildingException;

import java.io.IOException;
import java.net.ServerSocket;

public class EphemeralPortProvider implements PortProvider {
    @Override
    public int next() {
        try {
            final ServerSocket socket = new ServerSocket(0);
            socket.setReuseAddress(false);
            int port = socket.getLocalPort();
            socket.close();
            return port;
        } catch (IOException e) {
            //should not ever happen
            throw new RedisBuildingException("Could not provide ephemeral port", e);
        }
    }
}
