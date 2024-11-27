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
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.freese.spring.ribbon.myloadbalancer.ping.LoadBalancerPing;
import de.freese.spring.ribbon.myloadbalancer.ping.LoadBalancerPingNoOp;
import de.freese.spring.ribbon.myloadbalancer.strategy.LoadBalancerStrategy;
import de.freese.spring.ribbon.myloadbalancer.strategy.LoadBalancerStrategyRoundRobin;

/**
 * @author Thomas Freese
 */
@SuppressWarnings("unchecked")
public class LoadBalancer implements LoadBalancerPing {
    private static final Logger LOGGER = LoggerFactory.getLogger(LoadBalancer.class);

    /**
     * @author Thomas Freese
     */
    class Pinger extends TimerTask {
        @Override
        public void run() {
            LoggerFactory.getLogger(getClass()).debug("Pinger");

            final List<String> allServers = getAllServer();

            if (allServers.isEmpty()) {
                return;
            }

            final List<String> workingServers = pingWithCompletionService(allServers);
            // final List<String> workingServers = pingWithCompletableFuture(allServers);
            // final List<String> workingServers = pingWithStreams(allServers);
            // final List<String> workingServers = pingSequentiell(allServers);

            refreshAliveServer(workingServers);
        }

        /**
         * Sequentielle Pings.
         */
        List<String> pingSequentiell(final List<String> allServers) {
            final List<String> workingServers = new ArrayList<>();

            for (String server : allServers) {
                final boolean isAlive = isAlive(server);

                if (isAlive) {
                    workingServers.add(server);
                }
            }

            return workingServers;
        }

        /**
         * Parallele Pings mit dem {@link CompletableFuture}.
         */
        List<String> pingWithCompletableFuture(final List<String> allServers) {
            final List<String> workingServers = new ArrayList<>();

            final CompletableFuture<String>[] futures = allServers.stream()
                    .map(server -> CompletableFuture.supplyAsync(() -> isAlive(server) ? server : null))
                    .toArray(CompletableFuture[]::new);

            final CompletableFuture<Void> combinedFuture = CompletableFuture.allOf(futures);

            try {
                combinedFuture.get();

                for (CompletableFuture<String> cf : futures) {
                    try {
                        final String server = cf.get();

                        if (server != null) {
                            workingServers.add(server);
                        }
                    }
                    catch (InterruptedException ex) {
                        LOGGER.error(ex.getMessage());

                        // Restore interrupted state.
                        Thread.currentThread().interrupt();
                    }
                    catch (ExecutionException ex) {
                        LOGGER.error(ex.getMessage());
                    }
                }
            }
            catch (InterruptedException ex) {
                LOGGER.error(ex.getMessage());

                // Restore interrupted state.
                Thread.currentThread().interrupt();
            }
            catch (ExecutionException ex) {
                LOGGER.error(ex.getMessage());
            }

            return workingServers;
        }

        /**
         * Parallele Pings mit dem {@link ExecutorCompletionService}.
         */
        List<String> pingWithCompletionService(final List<String> allServers) {
            final List<String> workingServers = new ArrayList<>();

            final CompletionService<String> completionService = new ExecutorCompletionService<>(ForkJoinPool.commonPool());
            allServers.forEach(server -> completionService.submit(() -> isAlive(server) ? server : null));

            for (int i = 0; i < allServers.size(); ++i) {
                try {
                    final String server = completionService.take().get();

                    if (server != null) {
                        workingServers.add(server);
                    }
                }
                catch (InterruptedException ex) {
                    LOGGER.error(ex.getMessage());

                    // Restore interrupted state.
                    Thread.currentThread().interrupt();
                }
                catch (ExecutionException ex) {
                    LOGGER.error(ex.getMessage());
                }
            }

            return workingServers;
        }

        /**
         * Parallele Pings durch Streams.
         */
        List<String> pingWithStreams(final List<String> allServers) {
            return allServers.stream()
                    .parallel()
                    .map(server -> isAlive(server) ? server : null)
                    .filter(Objects::nonNull)
                    .toList()
                    ;
        }
    }

    private final List<String> aliveServer = new LinkedList<>();
    private final List<String> allServer = new LinkedList<>();
    private final ReentrantLock lock = new ReentrantLock(true);

    private LoadBalancerPing ping = new LoadBalancerPingNoOp();
    /**
     * Default 15 Sekunden.
     */
    private long pingDelay = TimeUnit.SECONDS.toMillis(15);

    private LoadBalancerStrategy strategy = new LoadBalancerStrategyRoundRobin();

    // private final ScheduledExecutorService scheduledExecutorService;

    private Timer timer;

    /**
     * @param server String[]; z.B. localhost:8080, localhost:8081
     */
    public LoadBalancer(final String... server) {
        // this(null, server);
        this(Executors.newSingleThreadScheduledExecutor(), server);
    }

    /**
     * @param scheduledExecutorService {@link ScheduledExecutorService}; Ohne diesen Service wird für das Pinkgen ein {@link javax.swing.Timer} verwendet.
     * @param server String[]; z.B. localhost:8080, localhost:8081
     */
    private LoadBalancer(final ScheduledExecutorService scheduledExecutorService, final String... server) {
        super();

        Objects.requireNonNull(server, "server required");

        for (String s : server) {
            addServer(s);
        }

        this.aliveServer.addAll(this.allServer);

        // this.scheduledExecutorService = scheduledExecutorService;

        setupPingTask();

        Runtime.getRuntime().addShutdownHook(new Thread(this::shutdown));
    }

    /**
     * Fügt einen weiteren Server hinzu.<br>
     * Dieser steht nach dem nächsten Ping-Intervall zur Verfügung, falls ansprechbar.<br>
     * Beispiel: "localhost:8082"
     */
    public void addServer(final String server) {
        if (!this.allServer.contains(server)) {
            this.allServer.add(server);
        }
    }

    /**
     * Liefert den nächsten Server.<br>
     */
    public String chooseServer() {
        return chooseServer(null);
    }

    /**
     * Liefert den nächsten Server.<br>
     *
     * @param key String, wird noch nicht berücksichtigt
     */
    public String chooseServer(final String key) {
        this.lock.lock();

        try {
            final String server = this.strategy.chooseServer(this.aliveServer, key);

            if (server == null) {
                throw new RuntimeException("no active server available");
            }

            return server;
        }
        finally {
            this.lock.unlock();
        }
    }

    public List<String> getAliveServer() {
        this.lock.lock();

        try {
            return Collections.unmodifiableList(this.aliveServer);
        }
        finally {
            this.lock.unlock();
        }
    }

    public List<String> getAllServer() {
        return Collections.unmodifiableList(this.allServer);
    }

    /**
     * Liefert die Zeit zwischen den Pings in Millisekunden.<br>
     * Default: 15 Sekunden = 15000 Millisekunden
     */
    public long getPingDelay() {
        return this.pingDelay;
    }

    @Override
    public boolean isAlive(final String server) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("pinging: {}", server);
        }

        try {
            return this.ping.isAlive(server);
        }
        catch (Exception ex) {
            LOGGER.error(ex.getMessage());
        }

        return false;
    }

    /**
     * Ersetzt den ServiceNamen durch einen aktiven Server.<br>
     * Beispiel: http://date-service/something -> http://localhost:8080/something
     */
    public URI reconstructURI(final String serviceName, final URI original) {
        String url = original.toString();
        final String nextServer = chooseServer();

        url = url.replace(serviceName, nextServer);

        try {
            return new URI(url);
        }
        catch (URISyntaxException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Setzt die Implementierung für den Ping.
     */
    public void setPing(final LoadBalancerPing ping) {
        this.ping = Objects.requireNonNull(ping, "ping required");
    }

    /**
     * Setzt die Zeit zwischen den Pings in Millisekunden.<br>
     * Default: 15 Sekunden = 15000 Millisekunden
     */
    public void setPingDelay(final long pingDelay) {
        if (pingDelay <= 0) {
            throw new IllegalArgumentException("pingDelay must be greater than 0");
        }

        final long old = this.pingDelay;

        this.pingDelay = pingDelay;

        if (this.pingDelay != old) {
            setupPingTask();
        }
    }

    /**
     * Setzt eine neue {@link LoadBalancerStrategy}.<br>
     * Default: {@link LoadBalancerStrategyRoundRobin}
     */
    public void setStrategy(final LoadBalancerStrategy strategy) {
        this.strategy = Objects.requireNonNull(strategy, "strategy required");
    }

    /**
     * Beenden des Pingers.
     */
    public void shutdown() {
        if (this.timer != null) {
            this.timer.cancel();
        }

        // if (this.scheduledExecutorService != null) {
        // this.scheduledExecutorService.shutdown();
        // }
    }

    /**
     * Aktualisiert die Liste der ansprechbaren Server.
     */
    protected void refreshAliveServer(final List<String> workingServers) {
        this.lock.lock();

        try {
            this.aliveServer.clear();
            this.aliveServer.addAll(workingServers);
        }
        finally {
            this.lock.unlock();
        }
    }

    /**
     * Startet den Ping-Task.
     */
    protected void setupPingTask() {
        // if (this.scheduledExecutorService != null) {
        // this.scheduledExecutorService.scheduleWithFixedDelay(new Pinger(), 3000L, getPingDelay(), TimeUnit.MILLISECONDS);
        // }
        // else {
        if (this.timer != null) {
            this.timer.cancel();
        }

        this.timer = new Timer(getClass().getSimpleName());
        this.timer.schedule(new Pinger(), 3000L, getPingDelay());
        // }
    }
}
