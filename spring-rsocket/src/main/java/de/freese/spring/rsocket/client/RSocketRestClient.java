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
 * curl localhost:8080/greet/error<br>
 *
 * @author Thomas Freese
 */
@RestController
@RequestMapping(path = "/greet")
@Profile("client")
public class RSocketRestClient
{
    /**
    *
    */
    private static final Logger LOGGER = LoggerFactory.getLogger(RSocketRestClient.class);

    /**
    *
    */
    private final RSocketRequester rSocketRequester;

    /**
     * Erstellt ein neues {@link RSocketRestClient} Object.
     *
     * @param rSocketRequester {@link RSocketRequester}
     */
    public RSocketRestClient(final RSocketRequester rSocketRequester)
    {
        super();

        this.rSocketRequester = Objects.requireNonNull(rSocketRequester, "rSocketRequester required");
    }

    /**
     * @param name String
     * @return {@link Flux}
     */
    @GetMapping(value = "channel/{name}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<GreetingResponse> channel(@PathVariable final String name)
    {
        LOGGER.info("Channel. Sending five requests. Waiting for five responses...");

       //@formatter:off
       return this.rSocketRequester
               .route("channel")
               .data(Flux.range(0, 5).map(integer -> new GreetingRequest(name, integer.longValue())), GreetingRequest.class)
               .retrieveFlux(GreetingResponse.class)
               ;
       //@formatter:on
    }

    /**
     * @return {@link Publisher}
     */
    @GetMapping("error")
    public Flux<GreetingResponse> error()
    {
        LOGGER.info("Error. Retrieve an Error...");

        // @formatter:off
        return this.rSocketRequester
               .route("error")
               .data(Mono.empty())
               .retrieveFlux(GreetingResponse.class);
        // @formatter:on
    }

    /**
     * @param name String
     * @return {@link Mono}
     */
    @GetMapping("fireAndForget/{name}")
    public Mono<Void> fireAndForget(@PathVariable final String name)
    {
        LOGGER.info("Fire-And-Forget. Sending one request. Expect no response (check server console log)...");

        //@formatter:off
        return this.rSocketRequester
                .route("fire-and-forget")
                .data(new GreetingRequest(name))
                .send()
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
        LOGGER.info("Sending one request. Waiting for one response...");

        //@formatter:off
        return  this.rSocketRequester
                .route("request-response")
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
        LOGGER.info("Request-Stream. Sending one request. Waiting for responses (Type 's' to stop)...");

       //@formatter:off
       return this.rSocketRequester
               .route("stream")
               .data(new GreetingRequest(name))
               .retrieveFlux(GreetingResponse.class)
               ;
       //@formatter:on
    }
}
