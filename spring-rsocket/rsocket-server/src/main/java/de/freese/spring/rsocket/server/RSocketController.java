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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import de.freese.spring.rsocket.server.data.MessageRequest;
import de.freese.spring.rsocket.server.data.MessageResponse;
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
    @PreAuthorize("hasRole('USER')")
    @MessageMapping("channel")
    public Flux<MessageResponse> channel(final Flux<MessageRequest> requests)
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
                .map(objects -> new MessageResponse(objects.getT2().getMessage() , objects.getT1()))
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
        // RSocketClientShell.ClientHandler
        requester.route("client-status")
                .data("OPEN")
                .retrieveFlux(Long.class)
                .doOnNext(value -> LOGGER.info("Client: {} Free Memory: {}.", client, value))
                .subscribe();
        // @formatter:on
    }

    /**
     * @return {@link MessageResponse}
     */
    @MessageMapping("error")
    public Mono<MessageResponse> error()
    {
        return Mono.error(new IllegalArgumentException("Bad Exception"));
    }

    /**
     * @param ex {@link Exception}
     * @return {@link Flux}
     */
    @MessageExceptionHandler
    public Mono<MessageResponse> errorHandler(final Exception ex)
    {
        MessageResponse response = new MessageResponse();
        response.setMessage(ex.getClass().getSimpleName() + ": " + ex.getMessage());

        return Mono.just(response);
    }

    /**
     * @param request {@link MessageRequest}
     * @return {@link Mono}
     */
    @PreAuthorize("hasRole('USER')")
    @MessageMapping("fire-and-forget")
    public Mono<Void> fireAndForget(final MessageRequest request)
    {
        LOGGER.info("Received fire-and-forget request: {}", request);

        return Mono.empty();
    }

    /**
     * @param name String
     * @return {@link Flux}
     */
    @PreAuthorize("hasRole('USER')")
    @MessageMapping("parameter/{name}")
    public Mono<MessageResponse> parameter(@DestinationVariable final String name)
    {
        LOGGER.info("Received parameter request: {}", name);

        return Mono.just(new MessageResponse(name));
    }

    /**
     * @param request {@link MessageRequest}
     * @return {@link MessageResponse}
     */
    @PreAuthorize("hasRole('USER')")
    @MessageMapping("request-response")
    public Mono<MessageResponse> requestResponse(final MessageRequest request)
    {
        LOGGER.info("Received request-response request: {}", request);

        return Mono.just(new MessageResponse(request.getMessage()));
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
     * @param request {@link MessageRequest}
     * @return {@link Flux}
     */
    @PreAuthorize("hasRole('USER')")
    @MessageMapping("stream")
    public Flux<MessageResponse> stream(final MessageRequest request)
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
                .map(objects -> new MessageResponse(request.getMessage(), objects.getT1()))
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
