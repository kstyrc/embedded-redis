package redis.embedded;

import redis.embedded.exceptions.EmbeddedRedisException;

import java.io.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.io.IOUtils;

abstract class AbstractRedisInstance implements Redis {
    protected List<String> args = Collections.emptyList();
    private volatile boolean active = false;
	private Process redisProcess;
    private final int port;

    private final ExecutorService executor = Executors.newFixedThreadPool(2);

    protected AbstractRedisInstance(int port) {
        this.port = port;
    }

    @Override
    public boolean isActive() {
        return active;
    }

	@Override
    public synchronized void start() throws EmbeddedRedisException {
        if (active) {
            throw new EmbeddedRedisException("This redis server instance is already running...");
        }
        try {
            redisProcess = createRedisProcessBuilder().start();
            awaitRedisServerReady();
            logErrors();
            active = true;
        } catch (IOException e) {
            throw new EmbeddedRedisException("Failed to start Redis instance", e);
        }
    }

    private void logErrors() {
        final InputStream errorStream = redisProcess.getErrorStream();
        Runnable errorStreamGobbler = new StreamGobbler(errorStream, System.err, "");
        executor.submit(errorStreamGobbler);
        
        final InputStream inputStream = redisProcess.getInputStream();
        Runnable inputStreamGobbler = new StreamGobbler(inputStream, System.err, "");
        executor.submit(inputStreamGobbler);
    }

    private void awaitRedisServerReady() throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(redisProcess.getInputStream()));
        try {
            String outputLine;
            do {
                outputLine = reader.readLine();
                if (outputLine == null) {
                    //Something goes wrong. Stream is ended before server was activated.
                    throw new RuntimeException("Can't start redis server. Check logs for details.");
                }
                System.out.println(outputLine);
            } while (!outputLine.matches(redisReadyPattern()));
        } finally {
            // IOUtils.closeQuietly(reader);
        }
    }

    protected abstract String redisReadyPattern();

    private ProcessBuilder createRedisProcessBuilder() {
        File executable = new File(args.get(0));
        ProcessBuilder pb = new ProcessBuilder(args);
        pb.directory(executable.getParentFile());
        return pb;
    }

    @Override
    public synchronized void stop() throws EmbeddedRedisException {
        if (active) {
            IOUtils.closeQuietly(redisProcess.getInputStream());
            IOUtils.closeQuietly(redisProcess.getOutputStream());
            IOUtils.closeQuietly(redisProcess.getErrorStream());
            redisProcess.destroy();
            tryWaitFor();
            active = false;
        }
    }

    private void tryWaitFor() {
        try {
            redisProcess.waitFor();
        } catch (InterruptedException e) {
            throw new EmbeddedRedisException("Failed to stop redis instance", e);
        }
    }

    @Override
    public List<Integer> ports() {
        return Arrays.asList(port);
    }

    private static class StreamGobbler implements Runnable {

        private final InputStream is;
        private final PrintStream os;
        private final String prefix;

        private StreamGobbler(InputStream is, PrintStream os, String prefix) {
            this.is = is;
            this.os = os;
            this.prefix = prefix;
        }

        @Override
        public void run() {
            try {
                InputStreamReader isr = new InputStreamReader(is);
                BufferedReader br = new BufferedReader(isr);
                String line=null;
                while ( (line = br.readLine()) != null) {
                    os.println(prefix + line);
                }
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
    }
}
