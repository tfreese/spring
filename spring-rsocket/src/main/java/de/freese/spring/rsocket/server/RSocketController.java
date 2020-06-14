/**
 * Created: 11.03.2020
 */

package de.freese.spring.rsocket.server;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.messaging.rsocket.annotation.ConnectMapping;
import org.springframework.stereotype.Controller;
import de.freese.spring.rsocket.GreetingRequest;
import de.freese.spring.rsocket.GreetingResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @author Thomas Freese
 */
@Controller
public class RSocketController
{
    /**
    *
    */
    private static final Logger LOGGER = LoggerFactory.getLogger(RSocketController.class);

    /**
     *
     */
    private final List<RSocketRequester> CLIENTS = new ArrayList<>();

    /**
     * Erstellt ein neues {@link RSocketController} Object.
     */
    public RSocketController()
    {
        super();
    }

    /**
     * @param requests {@link Flux}
     * @return {@link Flux}
     */
    @MessageMapping("greet/channel")
    public Flux<GreetingResponse> channel(final Flux<GreetingRequest> requests)
    {
        LOGGER.info("Received channel request (stream) at {}", Instant.now());

        // @formatter:off
        return requests
                // Indizierung
                .index()
                // Flux-Events der Requests loggen.
                .log()
                // Pro Element 1 Sekunde warten.
                .delayElements(Duration.ofSeconds(1))
                // Response-Objekt erzeugen.
                .map(objects -> new GreetingResponse(objects.getT2().getName() , objects.getT1()))
                // Flux-Events der Responses loggen.
                .log()
                ;
        // @formatter:on
    }

    /**
     * @param requester {@link RSocketRequester}
     * @param client String
     */
    @ConnectMapping("client-connect")
    void connectClientAndAskForTelemetry(final RSocketRequester requester, @Payload final String client)
    {
        // @formatter:off
        requester.rsocket()
                .onClose()
                .doFirst(() -> {
                    // Add all new clients to a client list.
                    LOGGER.info("Client: {} CONNECTED.", client);
                    this.CLIENTS.add(requester);
                })
                .doOnError(error -> {
                    // Warn when channels are closed by clients.
                    LOGGER.warn("Channel to client {} CLOSED", client);
                })
                .doFinally(consumer -> {
                    // Remove disconnected clients from the client list.
                    this.CLIENTS.remove(requester);
                    LOGGER.info("Client {} DISCONNECTED", client);
                })
                .subscribe();

        // Callback to client, confirming connection
        requester.route("client-status")
                .data("OPEN")
                .retrieveFlux(String.class)
                .doOnNext(s -> LOGGER.info("Client: {} Free Memory: {}.", client, s))
                .subscribe();
        // @formatter:on
    }

    /**
     * @return {@link GreetingResponse}
     */
    @MessageMapping("greet/error")
    public Mono<GreetingResponse> error()
    {
        return Mono.error(new IllegalArgumentException("Bad Exception"));
    }

    /**
     * @param iaex {@link IllegalArgumentException}
     * @return {@link Flux}
     */
    @MessageExceptionHandler
    public Mono<GreetingResponse> errorHandler(final IllegalArgumentException iaex)
    {
        return Mono.just(new GreetingResponse(iaex.getMessage()));
    }

    /**
     * @param request {@link GreetingRequest}
     * @return {@link Mono}
     */
    @MessageMapping("greet/fire-and-forget")
    public Mono<Void> fireAndForget(final GreetingRequest request)
    {
        LOGGER.info("Received fire-and-forget request: {}", request);

        return Mono.empty();
    }

    /**
     * @param name String
     * @return {@link Flux}
     */
    @MessageMapping("greet/parameter/{name}")
    public Mono<GreetingResponse> parameter(@DestinationVariable final String name)
    {
        LOGGER.info("Received parameter request: {}", name);

        return Mono.just(new GreetingResponse(name));
    }

    /**
     * @param request {@link GreetingRequest}
     * @return {@link GreetingResponse}
     */
    @MessageMapping("greet/request-response")
    public Mono<GreetingResponse> requestResponse(final GreetingRequest request)
    {
        LOGGER.info("Received request-response request: {}", request);

        return Mono.just(new GreetingResponse(request.getName()));
    }

    /**
     *
     */
    @PreDestroy
    void shutdown()
    {
        LOGGER.info("Detaching all remaining clients...");

        this.CLIENTS.stream().forEach(requester -> requester.rsocket().dispose());

        LOGGER.info("Shutting down.");
    }

    /**
     * @param request {@link GreetingRequest}
     * @return {@link Flux}
     */
    @MessageMapping("greet/stream")
    public Flux<GreetingResponse> stream(final GreetingRequest request)
    {
        LOGGER.info("Received stream request: {}", request);

        // @formatter:off
        return Flux
                // Jede Sekunde ein neues Element erzeugen.
                .interval(Duration.ofSeconds(1))
                // Nur die ersten 5 Elemente nehmen.
                .take(5L)
                // Indizierung
                .index()
                // Response-Objekt erzeugen.
                .map(objects -> new GreetingResponse(request.getName(), objects.getT1()))
                // Flux-Events loggen.
                .log()
                ;
        // @formatter:on

//        // @formatter:off
//        return Flux.fromStream(Stream.generate(() -> new GreetingResponse(request.getName())))
//                .take(5L)
//                .delayElements(Duration.ofSeconds(1))
//                ;
//        // @formatter:on
        //
//        // @formatter:off
//        return Flux.range(1, 5)
//                // Indizierung
//                .index()
//                // Response-Objekt erzeugen.
//                .map(objects -> new GreetingResponse(request.getName(), objects.getT1()))
//                // Eine Sekunde Pause
//                .delayElements(Duration.ofSeconds(1))
//                // Flux-Events loggen.
//                .log()
//                ;
//        // @formatter:on
    }
}