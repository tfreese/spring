// Created: 21.03.2018
package de.freese.spring.ribbon.myloadbalancer;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import de.freese.spring.ribbon.myloadbalancer.ping.LoadBalancerPing;
import de.freese.spring.ribbon.myloadbalancer.ping.LoadBalancerPingNoOp;
import de.freese.spring.ribbon.myloadbalancer.strategy.LoadBalancerStrategy;
import de.freese.spring.ribbon.myloadbalancer.strategy.LoadBalancerStrategyRoundRobin;

/**
 * @author Thomas Freese
 */
public class LoadBalancer implements LoadBalancerPing
{
    /**
     * @author Thomas Freese
     */
    class Pinger extends TimerTask
    {
        /**
         * Sequentielle Pings.
         *
         * @param allServers {@link List}
         * @return {@link List}
         */
        List<String> pingSequentiell(final List<String> allServers)
        {
            List<String> workingServers = new ArrayList<>();

            for (String server : allServers)
            {
                boolean isAlive = isAlive(server);

                if (isAlive)
                {
                    workingServers.add(server);
                }
            }

            return workingServers;
        }

        /**
         * Parallele Pings mit dem {@link CompletableFuture}.
         *
         * @param allServers {@link List}
         * @return {@link List}
         */
        List<String> pingWithCompletableFuture(final List<String> allServers)
        {
            List<String> workingServers = new ArrayList<>();

            // @formatter:off
            @SuppressWarnings("unchecked")
            CompletableFuture<String> [] futures = allServers.stream()
                .map(server -> CompletableFuture.supplyAsync(() -> isAlive(server) ? server : null))
                .toArray(CompletableFuture[]::new);
            // @formatter:on

            CompletableFuture<Void> combinedFuture = CompletableFuture.allOf(futures);

            try
            {
                combinedFuture.get();

                for (CompletableFuture<String> cf : futures)
                {
                    try
                    {
                        String server = cf.get();

                        if (server != null)
                        {
                            workingServers.add(server);
                        }
                    }
                    catch (InterruptedException | ExecutionException ex)
                    {
                        LOGGER.error(ex.getMessage());
                    }
                }
            }
            catch (InterruptedException | ExecutionException ex)
            {
                LOGGER.error(ex.getMessage());
            }

            return workingServers;
        }

        /**
         * Parallele Pings mir dem {@link ExecutorCompletionService}.
         *
         * @param allServers {@link List}
         * @return {@link List}
         */
        List<String> pingWithCompletionService(final List<String> allServers)
        {
            List<String> workingServers = new ArrayList<>();

            CompletionService<String> completionService = new ExecutorCompletionService<>(ForkJoinPool.commonPool());
            allServers.forEach(server -> completionService.submit(() -> isAlive(server) ? server : null));

            for (int i = 0; i < allServers.size(); ++i)
            {
                try
                {
                    String server = completionService.take().get();

                    if (server != null)
                    {
                        workingServers.add(server);
                    }
                }
                catch (InterruptedException | ExecutionException ex)
                {
                    LOGGER.error(ex.getMessage());
                }
            }

            return workingServers;
        }

        /**
         * Parallele Pings durch Streams.
         *
         * @param allServers {@link List}
         * @return {@link List}
         */
        List<String> pingWithStreams(final List<String> allServers)
        {
            // @formatter:off
            List<String> workingServers = allServers.stream()
                    .parallel()
                    .map(server -> isAlive(server) ? server : null)
                    .filter(server -> server != null)
                    .collect(Collectors.toList());
            // @formatter:on

            return workingServers;
        }

        /**
         * @see java.lang.Runnable#run()
         */
        @Override
        public void run()
        {
            LoggerFactory.getLogger(getClass()).debug("Pinger");

            List<String> allServers = getAllServer();

            if (allServers.isEmpty())
            {
                return;
            }

            List<String> workingServers = pingWithCompletionService(allServers);
            // List<String> workingServers = pingWithCompletableFuture(allServers);
            // List<String> workingServers = pingWithStreams(allServers);
            // List<String> workingServers = pingSequentiell(allServers);

            refreshAliveServer(workingServers);
        }
    }

    /**
     *
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(LoadBalancer.class);

    /**
    *
    */
    private final List<String> aliveServer = new LinkedList<>();

    /**
    *
    */
    private final List<String> allServer = new LinkedList<>();

    /**
     *
     */
    private final ReentrantLock lock = new ReentrantLock(true);

    /**
     *
     */
    private LoadBalancerPing ping = new LoadBalancerPingNoOp();

    /**
     * Default 15 Sekunden.
     */
    private long pingDelay = TimeUnit.SECONDS.toMillis(15);

    /**
     *
     */
    private LoadBalancerStrategy strategy = new LoadBalancerStrategyRoundRobin();

    // /**
    // *
    // */
    // private final ScheduledExecutorService scheduledExecutorService;

    /**
     *
     */
    private Timer timer;

    /**
     * Erzeugt eine neue Instanz von {@link LoadBalancer}.
     *
     * @param scheduledExecutorService {@link ScheduledExecutorService}; Ohne diesen Service wird für das Pinkgen ein {@link javax.swing.Timer} verwendet.
     * @param server String[]; z.B. localhost:8080, localhost:8081
     */
    private LoadBalancer(final ScheduledExecutorService scheduledExecutorService, final String...server)
    {
        super();

        Objects.requireNonNull(server, "server required");

        for (String s : server)
        {
            addServer(s);
        }

        this.aliveServer.addAll(this.allServer);

        // this.scheduledExecutorService = scheduledExecutorService;

        setupPingTask();

        Runtime.getRuntime().addShutdownHook(new Thread(this::shutdown));
    }

    /**
     * Erzeugt eine neue Instanz von {@link LoadBalancer}.
     *
     * @param server String[]; z.B. localhost:8080, localhost:8081
     */
    public LoadBalancer(final String...server)
    {
        this(null, server);
        // this(Executors.newSingleThreadScheduledExecutor(), server);
    }

    /**
     * Fügt einen weiteren Server hinzu.<br>
     * Dieser steht nach dem nächsten Ping-Intervall zur Verfügung, falls ansprechbar.<br>
     * Beispiel: "localhost:8082"
     *
     * @param server String
     */
    public void addServer(final String server)
    {
        if (!this.allServer.contains(server))
        {
            this.allServer.add(server);
        }
    }

    /**
     * Liefert den nächsten Server.<br>
     *
     * @return String
     */
    public String chooseServer()
    {
        return chooseServer(null);
    }

    /**
     * Liefert den nächsten Server.<br>
     *
     * @param key String; Wird noch nicht berücksichtigt
     * @return String
     */
    public String chooseServer(final String key)
    {
        this.lock.lock();

        try
        {
            String server = this.strategy.chooseServer(this.aliveServer, key);

            if (server == null)
            {
                throw new RuntimeException("no active server available");
            }

            return server;
        }
        finally
        {
            this.lock.unlock();
        }
    }

    /**
     * @return {@link List}
     */
    public List<String> getAliveServer()
    {
        this.lock.lock();

        try
        {
            return Collections.unmodifiableList(this.aliveServer);
        }
        finally
        {
            this.lock.unlock();
        }
    }

    /**
     * @return {@link List}
     */
    public List<String> getAllServer()
    {
        return Collections.unmodifiableList(this.allServer);
    }

    /**
     * Liefert die Zeit zwischen den Pings in Millisekunden.<br>
     * Default: 15 Sekunden = 15000 Millisekunden
     *
     * @return long
     */
    public long getPingDelay()
    {
        return this.pingDelay;
    }

    /**
     * @see de.freese.spring.ribbon.myloadbalancer.ping.LoadBalancerPing#isAlive(java.lang.String)
     */
    @Override
    public boolean isAlive(final String server)
    {
        if (LOGGER.isDebugEnabled())
        {
            LOGGER.debug("pinging: {}", server);
        }

        try
        {
            boolean isAlive = this.ping.isAlive(server);

            return isAlive;
        }
        catch (Exception ex)
        {
            LOGGER.error(ex.getMessage());
        }

        return false;
    }

    /**
     * Ersetzt den ServiceName durch einen aktiven Server.<br>
     * Beispiel: http://date-service/something -> http://localhost:8080/something
     *
     * @param serviceName String
     * @param original {@link URI}
     * @return {@link URI}
     */
    public URI reconstructURI(final String serviceName, final URI original)
    {
        String url = original.toString();
        String nextServer = chooseServer();

        url = url.replace(serviceName, nextServer);

        try
        {
            return new URI(url);
        }
        catch (URISyntaxException ex)
        {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Aktualisiert die Liste der ansprechbaren Server.
     *
     * @param workingServers {@link List}
     */
    protected void refreshAliveServer(final List<String> workingServers)
    {
        this.lock.lock();

        try
        {
            this.aliveServer.clear();
            this.aliveServer.addAll(workingServers);
        }
        finally
        {
            this.lock.unlock();
        }
    }

    /**
     * Setzt die Implementierung für den Ping.
     *
     * @param ping {@link LoadBalancerPing}
     */
    public void setPing(final LoadBalancerPing ping)
    {
        this.ping = Objects.requireNonNull(ping, "ping requried");
    }

    /**
     * Setzt die Zeit zwischen den Pings in Millisekunden.<br>
     * Default: 15 Sekunden = 15000 Millisekunden
     *
     * @param pingDelay long
     */
    public void setPingDelay(final long pingDelay)
    {
        if (pingDelay <= 0)
        {
            throw new IllegalArgumentException("pingDelay must be greater than 0");
        }

        long old = this.pingDelay;

        this.pingDelay = pingDelay;

        if (this.pingDelay != old)
        {
            setupPingTask();
        }
    }

    /**
     * Setzt eine neue {@link LoadBalancerStrategy}.<br>
     * Default: {@link LoadBalancerStrategyRoundRobin}
     *
     * @param strategy {@link LoadBalancerStrategy}
     */
    public void setStrategy(final LoadBalancerStrategy strategy)
    {
        this.strategy = Objects.requireNonNull(strategy, "strategy requried");
    }

    /**
     * Startet den Ping-Task.
     */
    protected void setupPingTask()
    {
        // if (this.scheduledExecutorService != null)
        // {
        // this.scheduledExecutorService.scheduleWithFixedDelay(new Pinger(), 3000L, getPingDelay(), TimeUnit.MILLISECONDS);
        // }
        // else
        // {

        if (this.timer != null)
        {
            this.timer.cancel();
        }

        this.timer = new Timer(getClass().getSimpleName());
        this.timer.schedule(new Pinger(), 3000L, getPingDelay());
        // }
    }

    /**
     * Beenden des Pingers.
     */
    public void shutdown()
    {
        if (this.timer != null)
        {
            this.timer.cancel();
        }

        // if (this.scheduledExecutorService != null)
        // {
        // this.scheduledExecutorService.shutdown();
        // }
    }
}
