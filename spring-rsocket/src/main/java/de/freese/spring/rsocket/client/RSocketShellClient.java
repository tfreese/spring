/**
 * Created: 11.03.2020
 */

package de.freese.spring.rsocket.client;

import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import de.freese.spring.rsocket.GreetingRequest;
import de.freese.spring.rsocket.GreetingResponse;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @author Thomas Freese
 */
@ShellComponent
@Profile("client & !test")
public class RSocketShellClient
{
    /**
    *
    */
    private static final Logger LOGGER = LoggerFactory.getLogger(RSocketShellClient.class);

    /**
     *
     */
    private Disposable disposable = null;

    /**
     *
     */
    private final RSocketRequester rSocketRequester;

    /**
     * Erstellt ein neues {@link RSocketShellClient} Object.
     *
     * @param rSocketRequester {@link RSocketRequester}
     */
    public RSocketShellClient(final RSocketRequester rSocketRequester)
    {
        super();

        this.rSocketRequester = Objects.requireNonNull(rSocketRequester, "rSocketRequester required");
    }

    /**
     *
     */
    @ShellMethod("Stream five requests. five responses (stream) will be printed.")
    public void channel()
    {
        LOGGER.info("Channel. Sending five requests. Waiting for five responses...");

        //@formatter:off
        this.rSocketRequester
                .route("channel")
                .data(Flux.range(0, 5).map(integer -> new GreetingRequest("Tommy", integer.longValue())), GreetingRequest.class)
                .retrieveFlux(GreetingResponse.class)
                .subscribe(gr -> LOGGER.info("Response received: {}", gr))
                ;
        //@formatter:on
    }

    /**
    *
    */
    @ShellMethod("Retrieve an Error.")
    public void error()
    {
        LOGGER.info("Error. Retrieve an Error...");

        // @formatter:off
        this.rSocketRequester
               .route("error")
               .data(Mono.empty())
               .retrieveFlux(GreetingResponse.class)
               .subscribe(gr -> LOGGER.info("Response received: {}", gr))
               ;
        // @formatter:on
    }

    /**
     * @throws InterruptedException Falls was schief geht.
     */
    @ShellMethod("Send one request. No response will be returned.")
    public void fireAndForget() throws InterruptedException
    {
        LOGGER.info("Fire-And-Forget. Sending one request. Expect no response (check server console log)...");

        //@formatter:off
        this.rSocketRequester
                .route("fire-and-forget")
                .data(new GreetingRequest("Tommy"))
                .send()
                .block()
                ;
        //@formatter:on
    }

    /**
     * @throws InterruptedException Falls was schief geht.
     */
    @ShellMethod("Send one request. One response will be printed.")
    public void requestResponse() throws InterruptedException
    {
        LOGGER.info("Sending one request. Waiting for one response...");

        //@formatter:off
        GreetingResponse message = this.rSocketRequester
                .route("request-response")
                .data(new GreetingRequest("Tommy"))
                .retrieveMono(GreetingResponse.class)
                .block()
                ;
        //@formatter:on

        LOGGER.info("\nResponse was: {}", message);
    }

    /**
     *
     */
    @ShellMethod("Stop streaming messages from the server.")
    public void s()
    {
        if (this.disposable != null)
        {
            this.disposable.dispose();
        }
    }

    /**
     *
     */
    @ShellMethod("Send one request. Many responses (stream) will be printed.")
    public void stream()
    {
        LOGGER.info("Request-Stream. Sending one request. Waiting for responses (Type 's' to stop)...");

        //@formatter:off
        this.disposable = this.rSocketRequester
                .route("stream")
                .data(new GreetingRequest("Tommy"))
                .retrieveFlux(GreetingResponse.class)
                .subscribe(gr -> LOGGER.info("New Response: {} (Type 's' to stop.)", gr))
                ;
        //@formatter:on
    }
}
