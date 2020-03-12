/**
 * Created: 11.03.2020
 */

package de.freese.spring.rsocket.server;

import java.time.Duration;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
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
    @MessageMapping("channel")
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
     * @return {@link GreetingResponse}
     */
    @MessageMapping("error")
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
    @MessageMapping("fire-and-forget")
    public Mono<Void> fireAndForget(final GreetingRequest request)
    {
        LOGGER.info("Received fire-and-forget request: {}", request);

        return Mono.empty();
    }

    /**
     * @param request {@link GreetingRequest}
     * @return {@link GreetingResponse}
     */
    @MessageMapping("request-response")
    public GreetingResponse requestResponse(final GreetingRequest request)
    {
        LOGGER.info("Received request-response request: {}", request);

        return new GreetingResponse(request.getName());
    }

    /**
     * @param request {@link GreetingRequest}
     * @return {@link Flux}
     */
    @MessageMapping("stream")
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
