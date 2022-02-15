// Created: 11.03.2020
package de.freese.spring.rsocket.client;

import java.time.Duration;
import java.util.Objects;
import java.util.UUID;

import javax.annotation.PreDestroy;

import de.freese.spring.rsocket.client.data.MessageRequest;
import de.freese.spring.rsocket.client.data.MessageResponse;
import io.rsocket.metadata.WellKnownMimeType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.messaging.rsocket.RSocketRequester.Builder;
import org.springframework.security.rsocket.metadata.UsernamePasswordMetadata;
import org.springframework.stereotype.Component;
import org.springframework.util.MimeTypeUtils;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @author Thomas Freese
 */
@Component
// @Profile("!test")
public class RSocketClient
{
    /**
     *
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(RSocketClient.class);
    /**
     *
     */
    private Disposable disposable;
    /**
     *
     */
    private RSocketRequester rsocketRequester;
    /**
     *
     */
    private final RSocketRequester.Builder rsocketRequesterBuilder;

    /**
     * Erstellt ein neues {@link RSocketClient} Object.
     *
     * @param builder {@link Builder}
     */
    public RSocketClient(final RSocketRequester.Builder builder)
    {
        super();

        this.rsocketRequesterBuilder = Objects.requireNonNull(builder, "builder required");
    }

    /**
     *
     */
    public void channel()
    {
        LOGGER.info("Channel (bi-directional streams), check server console log.");

        Mono<Duration> setting1 = Mono.just(Duration.ofSeconds(1));
        Mono<Duration> setting2 = Mono.just(Duration.ofSeconds(3)).delayElement(Duration.ofSeconds(5));
        Mono<Duration> setting3 = Mono.just(Duration.ofSeconds(5)).delayElement(Duration.ofSeconds(5));

        Flux<Duration> settings =
                Flux.concat(setting1, setting2, setting3).doOnNext(d -> LOGGER.info("Sending setting for a {}-second interval.", d.getSeconds()));

        //@formatter:off
        //this.disposable =
        this.rsocketRequester
                .route("channel")
                // Login-Infos bei jedem Request mitschicken.
                //.metadata( new UsernamePasswordMetadata(username, password), MimeTypeUtils.parseMimeType(WellKnownMimeType.MESSAGE_RSOCKET_AUTHENTICATION.getString()))
                .data(settings)
                .retrieveFlux(MessageResponse.class)
                //.subscribe(response -> LOGGER.info("Channel Received: {}", response))
                .doOnNext(response -> LOGGER.info("Channel Received: {}", response))
                .blockLast()
                ;
        //@formatter:on
    }

    /**
    *
    */
    public void error()
    {
        LOGGER.info("Request with Error, check server console log.");

        // @formatter:off
        this.rsocketRequester
               .route("error")
               .data(Mono.empty())
               .retrieveMono(MessageResponse.class)
               //.doOnError(ex ->  LOGGER.info("\nException: {}", ex))
               //.subscribe(response -> LOGGER.info("Error was: {}", response))
               .doOnNext(response -> LOGGER.info("Error was: {}", response))
               .block()
               ;
        // @formatter:on
    }

    /**
     * @throws InterruptedException Falls was schief geht.
     */
    public void fireAndForget() throws InterruptedException
    {
        LOGGER.info("Fire-And-Forget, check server console log.");

        // @formatter:off
        this.rsocketRequester
            .route("fire-and-forget")
            .data(new MessageRequest("me"))
            .send()
            .block()
            ;
        // @formatter:on
    }

    /**
     * RSocketController.connectClientAndAskForTelemetry
     *
     * @param username String
     * @param password String
     */
    public void login(final String username, final String password)
    {
        String clientId = UUID.randomUUID().toString();

        LOGGER.info("\nConnecting using client ID: {} and username: {}", clientId, username);

        UsernamePasswordMetadata user = new UsernamePasswordMetadata(username, password);

        // @formatter:off
        this.rsocketRequester = this.rsocketRequesterBuilder
                .setupMetadata(user, MimeTypeUtils.parseMimeType(WellKnownMimeType.MESSAGE_RSOCKET_AUTHENTICATION.getString()))
                .tcp("localhost", 7000)
                ;
        // @formatter:on
    }

    /**
     *
     */
    @PreDestroy
    public void logout()
    {
        stopStream();
        this.rsocketRequester.dispose();
        LOGGER.info("Logged out.");
    }

    /**
     * @throws InterruptedException Falls was schief geht.
     */
    public void parameter() throws InterruptedException
    {
        LOGGER.info("Request with Parameter, check server console log.");

        //@formatter:off
        this.rsocketRequester
            .route("parameter/me")
            .retrieveMono(MessageResponse.class)
            //.subscribe(response -> LOGGER.info("Parameter response was: {}", response))
            .doOnNext(response -> LOGGER.info("Parameter response was: {}", response))
            .block()
            ;
        //@formatter:on
    }

    /**
     * @throws InterruptedException Falls was schief geht.
     */
    public void requestResponse() throws InterruptedException
    {
        LOGGER.info("Request-Response, check server console log.");

        // @formatter:off
        this.rsocketRequester
            .route("request-response")
            .data(new MessageRequest("me"))
            .retrieveMono(MessageResponse.class)
            //.subscribe(response -> LOGGER.info("Request-Response was: {}", response))
            .doOnNext(response -> LOGGER.info("Request-Response was: {}", response))
            .block()
            ;
        // @formatter:on
    }

    /**
     *
     */
    public void startStream()
    {
        LOGGER.info("Stream, check server console log.");

        //@formatter:off
        //this.disposable =
        this.rsocketRequester
                .route("stream")
                .data(new MessageRequest("me"))
                .retrieveFlux(MessageResponse.class)
                //.subscribe(response -> LOGGER.info("Stream Response: {}", response))
                .doOnNext(response -> LOGGER.info("Stream Response: {}", response))
                .blockLast()
                ;
        //@formatter:on
    }

    /**
     *
     */
    public void stopStream()
    {
        if (this.disposable != null)
        {
            LOGGER.info("Stopping the current stream.");
            this.disposable.dispose();
            LOGGER.info("Stream stopped.");
            this.disposable = null;
        }
    }
}
