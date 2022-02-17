package de.freese.spring.rsocket;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Duration;
import java.util.Optional;

import de.freese.spring.rsocket.model.MessageRequest;
import de.freese.spring.rsocket.model.MessageResponse;
import io.rsocket.RSocket;
import io.rsocket.core.RSocketClient;
import io.rsocket.metadata.WellKnownMimeType;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.rsocket.context.LocalRSocketServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.messaging.rsocket.RSocketRequester.Builder;
import org.springframework.messaging.rsocket.RSocketStrategies;
import org.springframework.security.rsocket.metadata.UsernamePasswordMetadata;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.util.MimeType;
import org.springframework.util.MimeTypeUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Hooks;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

/**
 * @author Thomas Freese
 */
@SpringBootTest(properties = "spring.rsocket.server.port=0", webEnvironment = WebEnvironment.RANDOM_PORT)
// Wird für den reinen Client nicht benötigt.
// , exclude =
// {
// ReactiveUserDetailsServiceAutoConfiguration.class,
// SecurityAutoConfiguration.class,
// ReactiveSecurityAutoConfiguration.class,
// RSocketSecurityAutoConfiguration.class
// }
@ActiveProfiles(
{
        "test", "simple"
})
class RSocketClientToServerTest
{
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
     * @param strategies {@link RSocketStrategies}
     * @param host String
     * @param port int
     */
    @BeforeAll
    public static void beforeAll(@Autowired final RSocketRequester.Builder builder, @Autowired final RSocketStrategies strategies,
                                 @Value("${spring.rsocket.server.address}") final String host, @LocalRSocketServerPort final int port)
    {
        // Fehlermeldung, wenn Client die Verbindung schliesst.
        // Nur einmalig definieren, sonst gibs mehrere Logs-Meldungen !!!
        // Hooks.onErrorDropped(th -> LOGGER.warn(th.getMessage()));
        Hooks.onErrorDropped(th -> {
            // Empty
        });

        UsernamePasswordMetadata credentials = new UsernamePasswordMetadata("user", "pass");
        MimeType mimeType = MimeTypeUtils.parseMimeType(WellKnownMimeType.MESSAGE_RSOCKET_AUTHENTICATION.getString());

        // .setupMetadata(token, BearerTokenMetadata.BEARER_AUTHENTICATION_MIME_TYPE)

        // @formatter:off
        requester = builder
                .setupMetadata(credentials, mimeType)
                .tcp(host, port)
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
        Mono<Void> result = requester.route("fire-and-forget").data(new MessageRequest("Fire-And-Forget")).retrieveMono(Void.class);

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
        MessageRequest request = new MessageRequest("Request");

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
        MessageRequest request = new MessageRequest("Stream");

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
