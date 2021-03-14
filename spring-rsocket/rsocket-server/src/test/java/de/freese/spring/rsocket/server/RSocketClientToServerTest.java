package de.freese.spring.rsocket.server;

import static org.junit.jupiter.api.Assertions.assertEquals;
import java.time.Duration;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.rsocket.context.LocalRSocketServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.messaging.rsocket.RSocketRequester.Builder;
import org.springframework.messaging.rsocket.RSocketStrategies;
import org.springframework.messaging.rsocket.annotation.support.RSocketMessageHandler;
import org.springframework.security.rsocket.metadata.SimpleAuthenticationEncoder;
import org.springframework.security.rsocket.metadata.UsernamePasswordMetadata;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.util.MimeType;
import org.springframework.util.MimeTypeUtils;
import de.freese.spring.rsocket.server.data.MessageRequest;
import de.freese.spring.rsocket.server.data.MessageResponse;
import io.rsocket.RSocket;
import io.rsocket.SocketAcceptor;
import io.rsocket.core.RSocketClient;
import io.rsocket.metadata.WellKnownMimeType;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

/**
 * @author Thomas Freese
 */
@SpringBootTest(properties = "spring.rsocket.server.port=0", webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class RSocketClientToServerTest
{
    /**
     * @author Thomas Freese
     */
    static class ClientHandler
    {
        /**
         * @param status String
         * @return {@link Publisher}
         */
        @MessageMapping("client-status")
        public Publisher<Long> statusUpdate(final String status)
        {
            LOGGER.info("Connection {}", status);

            return Flux.interval(Duration.ofSeconds(3)).map(index -> Runtime.getRuntime().freeMemory());
        }
    }

    /**
     *
     */
    private static UsernamePasswordMetadata credentials;

    /**
    *
    */
    private static final Logger LOGGER = LoggerFactory.getLogger(RSocketClientToServerTest.class);

    /**
     *
     */
    private static MimeType mimeType;

    /**
     *
     */
    private static RSocketRequester requester;

    /**
     *
     */
    @AfterAll
    public static void afterAll()
    {
        Optional.ofNullable(requester.rsocketClient()).ifPresent(RSocketClient::dispose);
        Optional.ofNullable(requester.rsocket()).ifPresent(RSocket::dispose);
    }

    /**
     * @param builder {@link Builder}
     * @param port int
     * @param strategies {@link RSocketStrategies}
     */
    @BeforeAll
    public static void setupOnce(@Autowired final RSocketRequester.Builder builder, @LocalRSocketServerPort final int port,
                                 @Autowired final RSocketStrategies strategies)
    {
        SocketAcceptor responder = RSocketMessageHandler.responder(strategies, new ClientHandler());
        credentials = new UsernamePasswordMetadata("user", "pass");
        mimeType = MimeTypeUtils.parseMimeType(WellKnownMimeType.MESSAGE_RSOCKET_AUTHENTICATION.getString());

        // @formatter:off
        requester = builder.setupRoute("client-connect")
                .setupData(UUID.randomUUID().toString())
                .setupMetadata(credentials, mimeType)

                // Wird für Login/Security benötigt.
                .rsocketStrategies(b -> b.encoder(new SimpleAuthenticationEncoder()))
                //.rsocketStrategies(this.rsocketStrategies) // Für Verbindung ohne Login/Security.

                .rsocketConnector(connector -> connector.acceptor(responder))
                .tcp("localhost", port)
                ;
        // @formatter:on
    }

    /**
     *
     */
    @Test
    void testFireAndForget()
    {
        // Send a fire-and-forget message
        Mono<Void> result = requester.route("fire-and-forget").data(new MessageRequest("TEST - Fire-And-Forget")).retrieveMono(Void.class);

        // Assert that the result is a completed Mono.
        StepVerifier.create(result).verifyComplete();
    }

    /**
     *
     */
    @Test
    void testNoMatchingRouteGetsException()
    {
        // Send a request with bad route and data
        Mono<String> result = requester.route("invalid").data("anything").retrieveMono(String.class);

        // Verify that an error is generated
        StepVerifier.create(result).expectErrorMessage("No handler for destination 'invalid'").verify(Duration.ofSeconds(5));
    }

    /**
     *
     */
    @Test
    void testRequestGetsResponse()
    {
        MessageRequest request = new MessageRequest("TEST - Request");

        // Send a request message
        Mono<MessageResponse> result = requester.route("request-response").data(request).retrieveMono(MessageResponse.class);

        // Verify that the response message contains the expected data
        StepVerifier.create(result).consumeNextWith(response -> {
            assertEquals("Hello " + request.getMessage(), response.getMessage());
            assertEquals(0, response.getIndex());
        }).verifyComplete();
    }

    /**
     *
     */
    @Test
    void testRequestGetsStream()
    {
        MessageRequest request = new MessageRequest("TEST - Stream");

        // Send a request message
        Flux<MessageResponse> result = requester.route("stream").data(request).retrieveFlux(MessageResponse.class);

        // Verify that the response messages contain the expected data
        StepVerifier.create(result).consumeNextWith(response -> {
            assertEquals("Hello " + request.getMessage(), response.getMessage());
            assertEquals(0, response.getIndex());
        }).expectNextCount(3).consumeNextWith(response -> {
            assertEquals("Hello " + request.getMessage(), response.getMessage());
            assertEquals(4, response.getIndex());
        }).thenCancel().verify();
    }

    /**
     *
     */
    @Test
    void testStreamGetsStream()
    {
        Mono<Duration> setting1 = Mono.just(Duration.ofSeconds(3)).delayElement(Duration.ofSeconds(0));
        Mono<Duration> setting2 = Mono.just(Duration.ofSeconds(3)).delayElement(Duration.ofSeconds(2));
        Flux<Duration> settings = Flux.concat(setting1, setting2);

        // Send a stream of request messages
        Flux<MessageResponse> result = requester.route("channel").data(settings).retrieveFlux(MessageResponse.class);

        // Verify that the response messages contain the expected data
        StepVerifier.create(result).consumeNextWith(response -> {
            assertEquals("Hello PT3S", response.getMessage());
            assertEquals(0, response.getIndex());
        }).consumeNextWith(response -> {
            assertEquals("Hello PT3S", response.getMessage());
            assertEquals(1, response.getIndex());
        }).thenCancel().verify();
    }
}
