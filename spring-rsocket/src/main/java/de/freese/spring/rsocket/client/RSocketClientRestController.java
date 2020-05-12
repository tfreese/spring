/**
 * Created: 11.03.2020
 */

package de.freese.spring.rsocket.client;

import java.util.Objects;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.security.rsocket.metadata.UsernamePasswordMetadata;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import de.freese.spring.rsocket.GreetingRequest;
import de.freese.spring.rsocket.GreetingResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * curl localhost:8080/greet/fireAndForget/tommy<br>
 * curl localhost:8080/greet/requestResponse/tommy<br>
 * curl localhost:8080/greet/stream/tommy<br>
 * curl localhost:8080/greet/channel/tommy<br>
 * curl localhost:8080/greet/parameter/tommy<br>
 * curl localhost:8080/greet/error<br>
 *
 * @author Thomas Freese
 */
@RestController
@RequestMapping("greet")
@Profile("client")
public class RSocketClientRestController
{
    /**
     *
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(RSocketClientRestController.class);

    /**
     *
     */
    private static final UsernamePasswordMetadata REQUEST_CREDENTIALS = new UsernamePasswordMetadata("tommy", "gehaim");

    /**
     *
     */
    private final RSocketRequester rSocketRequester;

    /**
     * Erstellt ein neues {@link RSocketClientRestController} Object.
     *
     * @param rSocketRequester {@link RSocketRequester}
     */
    public RSocketClientRestController(final RSocketRequester rSocketRequester)
    {
        super();

        this.rSocketRequester = Objects.requireNonNull(rSocketRequester, "rSocketRequester required");

        // @formatter:off
        this.rSocketRequester.rsocket()
            .onClose()
            .doOnError(error -> LOGGER.warn("Connection CLOSED"))
            .doFinally(consumer -> LOGGER.info("Client DISCONNECTED"))
            .subscribe()
        ;
        // @formatter:on
    }

    /**
     * @param name String
     * @return {@link Flux}
     */
    @GetMapping(value = "channel/{name}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<GreetingResponse> channel(@PathVariable final String name)
    {
        LOGGER.info("Channel: sending five requests, waiting for five responses...");

       //@formatter:off
       return this.rSocketRequester
               .route("greet/channel")
               .metadata(REQUEST_CREDENTIALS, UsernamePasswordMetadata.BASIC_AUTHENTICATION_MIME_TYPE)
               .data(Flux.range(0, 5).map(integer -> new GreetingRequest(name, integer.longValue())), GreetingRequest.class)
               .retrieveFlux(GreetingResponse.class)
               ;
       //@formatter:on
    }

    /**
     * @return {@link Publisher}
     */
    @GetMapping("error")
    public Mono<GreetingResponse> error()
    {
        LOGGER.info("Error: retrieve an Error...");

        // @formatter:off
        return this.rSocketRequester
               .route("greet/error")
               .metadata(REQUEST_CREDENTIALS, UsernamePasswordMetadata.BASIC_AUTHENTICATION_MIME_TYPE)
               .data(Mono.empty())
               .retrieveMono(GreetingResponse.class);
        // @formatter:on
    }

    /**
     * @param name String
     * @return {@link Mono}
     */
    @GetMapping("fireAndForget/{name}")
    public Mono<Void> fireAndForget(@PathVariable final String name)
    {
        LOGGER.info("Fire-And-Forget: sending one request, expect no response (check server console log)...");

        //@formatter:off
        return this.rSocketRequester
                .route("greet/fire-and-forget")
                .metadata(REQUEST_CREDENTIALS, UsernamePasswordMetadata.BASIC_AUTHENTICATION_MIME_TYPE)
                .data(new GreetingRequest(name))
                .send()
                ;
        //@formatter:on
    }

    /**
     * @param name String
     * @return {@link Mono}
     */
    @GetMapping("parameter/{name}")
    public Mono<GreetingResponse> parameter(@PathVariable final String name)
    {
        LOGGER.info("Parameter: Sending one request, waiting for one response...");

        //@formatter:off
        return  this.rSocketRequester
                .route("greet/parameter/" + name)
                .metadata(REQUEST_CREDENTIALS, UsernamePasswordMetadata.BASIC_AUTHENTICATION_MIME_TYPE)
                .retrieveMono(GreetingResponse.class)
                ;
        //@formatter:on
    }

    /**
     * @param name String
     * @return {@link Mono}
     */
    @GetMapping("requestResponse/{name}")
    public Mono<GreetingResponse> requestResponse(@PathVariable final String name)
    {
        LOGGER.info("Request-Response: sending one request, waiting for one response...");

        //@formatter:off
        return  this.rSocketRequester
                .route("greet/request-response")
                .metadata(REQUEST_CREDENTIALS, UsernamePasswordMetadata.BASIC_AUTHENTICATION_MIME_TYPE)
                .data(new GreetingRequest(name))
                .retrieveMono(GreetingResponse.class)
                ;
        //@formatter:on
    }

    /**
     * @param name String
     * @return {@link Flux}
     */
    @GetMapping(value = "/stream/{name}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<GreetingResponse> stream(@PathVariable final String name)
    {
        LOGGER.info("Stream: sending one request, waiting for responses...");

       //@formatter:off
       return this.rSocketRequester
               .route("greet/stream")
               .metadata(REQUEST_CREDENTIALS, UsernamePasswordMetadata.BASIC_AUTHENTICATION_MIME_TYPE)
               .data(new GreetingRequest(name))
               .retrieveFlux(GreetingResponse.class)
               ;
       //@formatter:on
    }
}
