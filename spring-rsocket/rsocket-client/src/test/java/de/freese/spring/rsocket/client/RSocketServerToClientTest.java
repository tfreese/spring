package de.freese.spring.rsocket.client;

import static org.assertj.core.api.Assertions.assertThat;
import java.time.Duration;
import java.util.UUID;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.messaging.rsocket.RSocketStrategies;
import org.springframework.messaging.rsocket.annotation.ConnectMapping;
import org.springframework.messaging.rsocket.annotation.support.RSocketMessageHandler;
import org.springframework.stereotype.Controller;
import io.rsocket.SocketAcceptor;
import io.rsocket.core.RSocketServer;
import io.rsocket.frame.decoder.PayloadDecoder;
import io.rsocket.transport.netty.server.CloseableChannel;
import io.rsocket.transport.netty.server.TcpServerTransport;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.MonoProcessor;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;

/**
 * @author Thomas Freese
 */
class RSocketServerToClientTest
{
    /**
     * @author Thomas Freese
     */
    private static class ClientHandler
    {
        /**
         * @param status String
         * @return {@link Publisher}
         */
        @MessageMapping("client-status")
        public Publisher<String> statusUpdate(final String status)
        {
            LOGGER.info("Connection {}", status);

            // return Flux.interval(Duration.ofSeconds(3)).map(index -> Long.toString(Runtime.getRuntime().freeMemory()));
            return Mono.delay(Duration.ofSeconds(3)).map(index -> Long.toString(Runtime.getRuntime().freeMemory()));
        }
    }

    /**
     * This test-specific configuration allows Spring to help configure our test environment. These beans will be placed into the Spring context and can be
     * accessed when required.
     */
    @TestConfiguration
    static class ServerConfig
    {
        /**
         * @return {@link RSocketStrategies}
         */
        @Bean("testStrategies")
        public RSocketStrategies rsocketStrategies()
        {
            return RSocketStrategies.create();
        }

        /**
         * @return {@link ServerController}
         */
        @Bean
        public ServerController serverController()
        {
            return new ServerController();
        }

        /**
         * @param strategies {@link RSocketStrategies}
         * @return {@link RSocketMessageHandler}
         */
        @Bean
        public RSocketMessageHandler serverMessageHandler(@Qualifier("testStrategies") final RSocketStrategies strategies)
        {
            RSocketMessageHandler handler = new RSocketMessageHandler();
            handler.setRSocketStrategies(strategies);

            return handler;
        }
    }

    /**
     * Fake Spring @Controller class which is a stand-in 'test rig' for our real server. It contains a custom @ConnectMapping that tests if our ClientHandler is
     * responding to server-side calls for telemetry data.
     */
    @Controller
    static class ServerController
    {
        /**
         * volatile guarantees visibility across threads.<br>
         * MonoProcessor implements stateful semantics for a mono.
         */
        volatile MonoProcessor<Object> result;

        /**
         * Allow some time for the test to execute
         *
         * @param duration {@link Duration}
         */
        public void await(final Duration duration)
        {
            this.result.block(duration);
        }

        /**
         * Reset the stateful Mono
         */
        public void reset()
        {
            this.result = MonoProcessor.create();
        }

        /**
         * Run the provided test, collecting the results into a stateful Mono.
         *
         * @param test {@link Runnable}
         */
        private void runTest(final Runnable test)
        {
            // Run the test provided
            // @formatter:off
            Mono.fromRunnable(test)
                    .doOnError(ex -> this.result.onError(ex)) // test result was an error
                    .doOnSuccess(o -> this.result.onComplete()) // test result was success
                    .subscribeOn(Schedulers.boundedElastic()) // StepVerifier will block
                    .subscribe();
            // @formatter:on
        }

        /**
         * Test method. When a client connects to this server, ask the client for its telemetry data and test that the telemetry received is within a good
         * range.
         *
         * @param requester {@link RSocketRequester}
         * @param client String
         */
        @ConnectMapping("client-connect")
        void verifyConnectShellClientAndAskForTelemetry(final RSocketRequester requester, @Payload final String client)
        {

            // test the client's message payload contains the expected client ID
            assertThat(client).isNotNull();
            assertThat(client).isNotEmpty();
            assertThat(client).isEqualTo(clientId);
            LOGGER.info("************** CONNECTION - Client ID: {}", client);

            // @formatter:off
            runTest(() -> {
                Flux<String> flux = requester
                        .route("client-status") // Test the 'client-status' message handler mapping
                        .data("OPEN") // confirm to the client th connection is open
                        .retrieveFlux(String.class); // ask the client for its telemetry

                StepVerifier.create(flux)
                        .consumeNextWith(value -> {
                            // assert the memory reading is in the 'good' range
                            assertThat(value).isNotNull();
                            assertThat(value).isNotEmpty();
                            assertThat(Long.valueOf(value)).isPositive();
                            assertThat(Long.valueOf(value)).isGreaterThan(0);
                        })
                        .thenCancel()
                        .verify(Duration.ofSeconds(10));
            });
            // @formatter:on
        }
    }

    /**
     *
     */
    private static String clientId = null;

    /**
     *
     */
    private static AnnotationConfigApplicationContext context = null;

    /**
    *
    */
    private static final Logger LOGGER = LoggerFactory.getLogger(RSocketServerToClientTest.class);

    /**
     *
     */
    private static CloseableChannel server = null;

    /**
     *
     */
    @BeforeAll
    public static void setupOnce()
    {
        // create a client identity spring for this test suite
        clientId = UUID.randomUUID().toString();

        // create a Spring context for this test suite and obtain some beans
        context = new AnnotationConfigApplicationContext(ServerConfig.class);

        // Create an RSocket server for use in testing
        RSocketMessageHandler messageHandler = context.getBean(RSocketMessageHandler.class);

        // @formatter:off
        server = RSocketServer.create(messageHandler.responder())
                .payloadDecoder(PayloadDecoder.ZERO_COPY)
                .bind(TcpServerTransport.create("localhost", 0))
                .block();
        // @formatter:on
    }

    /**
     *
     */
    @AfterAll
    public static void tearDownOnce()
    {
        server.dispose();
    }

    /**
     * This private method is used to establish a connection to our fake RSocket server. It also controls the state of our test controller. This method is
     * reusable by many tests.
     *
     * @param connectionRoute String
     */
    private void connectAndRunTest(final String connectionRoute)
    {
        ServerController controller = context.getBean(ServerController.class);
        RSocketStrategies strategies = context.getBean(RSocketStrategies.class);
        RSocketRequester requester = null;

        try
        {
            controller.reset();

            // Add our ClientHandler as a responder
            SocketAcceptor responder = RSocketMessageHandler.responder(strategies, new ClientHandler());

            // Create an RSocket requester that includes our responder
            // @formatter:off
            requester = RSocketRequester.builder()
                    .setupRoute(connectionRoute)
                    .setupData(clientId)
                    .rsocketStrategies(strategies)
                    .rsocketConnector(connector -> connector.acceptor(responder))
                    .tcp("localhost", server.address().getPort())
                    ;
            // @formatter:on

            // Give the test time to run, wait for the server's call.
            controller.await(Duration.ofSeconds(10));
        }
        finally
        {
            if (requester != null)
            {
                requester.rsocket().dispose();
            }
        }
    }

    /**
     * Test that our client-side 'ClientHandler' class responds to server sent messages correctly.
     */
    @Test
    void testServerCallsClientAfterConnection()
    {
        connectAndRunTest("client-connect");
    }
}
