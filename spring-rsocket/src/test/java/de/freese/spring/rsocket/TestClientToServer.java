package de.freese.spring.rsocket;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Duration;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.messaging.rsocket.RSocketRequester;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import de.freese.spring.rsocket.model.MessageRequest;
import de.freese.spring.rsocket.model.MessageResponse;

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
interface TestClientToServer {
    RSocketRequester getRequester();

    @Test
    default void testChannel() {
        final Mono<Duration> setting1 = Mono.just(Duration.ofSeconds(3)).delayElement(Duration.ofSeconds(0));
        final Mono<Duration> setting2 = Mono.just(Duration.ofSeconds(3)).delayElement(Duration.ofSeconds(2));
        final Flux<Duration> settings = Flux.concat(setting1, setting2);

        // Send a stream of request messages
        final Flux<MessageResponse> result = getRequester().route("channel").data(settings).retrieveFlux(MessageResponse.class);

        // Verify that the response messages contain the expected data
        StepVerifier.create(result).consumeNextWith(response -> {
            assertEquals("Hello PT3S", response.getMessage());
            assertEquals(0, response.getIndex());
        }).consumeNextWith(response -> {
            assertEquals("Hello PT3S", response.getMessage());
            assertEquals(1, response.getIndex());
        }).thenCancel().verify();
    }

    @Test
    default void testFireAndForget() {
        // Send a fire-and-forget message
        final Mono<Void> result = getRequester().route("fire-and-forget").data(new MessageRequest("Fire-And-Forget")).retrieveMono(Void.class);

        // Assert that the result is a completed Mono.
        StepVerifier.create(result).verifyComplete();
    }

    @Test
    default void testNoMatchingRoute() {
        // Send a request with bad route and data
        final Mono<String> result = getRequester().route("invalid").data("anything").retrieveMono(String.class);

        // Verify that an error is generated
        StepVerifier.create(result).expectErrorMessage("No handler for destination 'invalid'").verify(Duration.ofSeconds(5));
    }

    @Test
    default void testRequestResponse() {
        final MessageRequest request = new MessageRequest("Request");

        // Send a request message
        final Mono<MessageResponse> result = getRequester().route("request-response").data(request).retrieveMono(MessageResponse.class);

        // Verify that the response message contains the expected data
        StepVerifier.create(result).consumeNextWith(response -> {
            assertEquals("Hello " + request.getMessage(), response.getMessage());
            assertEquals(0, response.getIndex());
        }).verifyComplete();
    }

    @Test
    default void testStream() {
        final MessageRequest request = new MessageRequest("Stream");

        // Send a request message
        final Flux<MessageResponse> result = getRequester().route("stream").data(request).retrieveFlux(MessageResponse.class);

        // Verify that the response messages contain the expected data
        StepVerifier.create(result).consumeNextWith(response -> {
            assertEquals("Hello " + request.getMessage(), response.getMessage());
            assertEquals(0, response.getIndex());
        }).expectNextCount(1).consumeNextWith(response -> {
            assertEquals("Hello " + request.getMessage(), response.getMessage());
            assertEquals(2, response.getIndex());
        }).thenCancel().verify();
    }
}
