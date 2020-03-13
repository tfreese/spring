/**
 * Created: 11.03.2020
 */

package de.freese.spring.rsocket.client;

import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClient.Builder;
import de.freese.spring.rsocket.GreetingResponse;

/**
 * @author Thomas Freese
 */
@ShellComponent
@Profile("client & !test")
public class RSocketClientShell
{
    /**
    *
    */
    private static final Logger LOGGER = LoggerFactory.getLogger(RSocketClientShell.class);

    /**
     *
     */
    private final WebClient webClient;

    /**
     * Erstellt ein neues {@link RSocketClientShell} Object.
     *
     * @param webClientBuilder {@link Builder}
     */
    public RSocketClientShell(final WebClient.Builder webClientBuilder)
    {
        super();

        this.webClient = Objects.requireNonNull(webClientBuilder, "webClientBuilder required").build();
    }

    /**
     *
     */
    @ShellMethod("Stream five requests. five responses (stream) will be printed.")
    public void channel()
    {
        LOGGER.info("Channel: sending five requests, waiting for five responses...");

        //@formatter:off
        this.webClient
                .get()
                //.uri("greet/channel/Tommy")
                .uri(uriBuilder -> uriBuilder
                        .path("greet/channel/{name}")
                        .build("Tommy")
                )
                //.accept(MediaType.APPLICATION_JSON) // Wirft bei '#channel' und '#stream' Fehler: 406 Not Acceptable from GET http://localhost:8080/greet/channel/tommy
                //.acceptCharset(StandardCharsets.UTF_8)
                //.exchange().flatMap(clientResponse -> clientResponse.toEntity(GreetingResponse.class)) // Liefert Header, Status und ResponseBody.
                .retrieve().bodyToFlux(GreetingResponse.class) // Liefert nur den ResponseBody.
                .subscribe(gr -> LOGGER.info("Response received: {}", gr))
                ;
        // @formatter:on
    }

    /**
    *
    */
    @ShellMethod("Retrieve an Error.")
    public void error()
    {
        LOGGER.info("Error: retrieve an Error...");

        //@formatter:off
        this.webClient
                .get()
                .uri("greet/error")
                .retrieve()
                .bodyToMono(GreetingResponse.class)
                .subscribe(gr -> LOGGER.info("Response was: {}", gr))
                ;
        // @formatter:on
    }

    /**
     * @throws InterruptedException Falls was schief geht.
     */
    @ShellMethod("Send one request. No response will be returned.")
    public void fireAndForget() throws InterruptedException
    {
        LOGGER.info("Fire-And-Forget: sending one request, expect no response (check server console log)...");

        //@formatter:off
        this.webClient
                .get()
                .uri("greet/fireAndForget/{name}", "Tommy")
                .retrieve()
                .bodyToMono(Void.class)
                .block()
                ;
        // @formatter:on
    }

    /**
     * @throws InterruptedException Falls was schief geht.
     */
    @ShellMethod("Send one request. One response will be printed.")
    public void parameter() throws InterruptedException
    {
        LOGGER.info("Parameter: Sending one request, waiting for one response...");

        //@formatter:off
        this.webClient
                .get()
                .uri("greet/parameter/{name}", "Tommy")
                .retrieve()
                .bodyToMono(GreetingResponse.class)
                .subscribe(gr -> LOGGER.info("Response was: {}", gr))
                ;
        // @formatter:on
    }

    /**
     * @throws InterruptedException Falls was schief geht.
     */
    @ShellMethod("Send one request. One response will be printed.")
    public void requestResponse() throws InterruptedException
    {
        LOGGER.info("Request-Response: sending one request, waiting for one response...");

        //@formatter:off
        this.webClient
                .get()
                .uri("greet/requestResponse/{name}", "Tommy")
                .retrieve()
                .bodyToMono(GreetingResponse.class)
                .subscribe(gr -> LOGGER.info("Response was: {}", gr))
                ;
        // @formatter:on
    }

    /**
     *
     */
    @ShellMethod("Send one request. Many responses (stream) will be printed.")
    public void stream()
    {
        LOGGER.info("Stream: sending one request, waiting for responses...");

        //@formatter:off
        this.webClient
                .get()
                .uri("greet/stream/{name}", "Tommy")
                .retrieve()
                .bodyToFlux(GreetingResponse.class)
                .subscribe(gr -> LOGGER.info("Response received: {}", gr))
                ;
        // @formatter:on
    }
}
