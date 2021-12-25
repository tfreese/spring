// Created: 11.03.2020
package de.freese.spring.rsocket.client;

import java.time.Duration;
import java.util.Objects;
import java.util.UUID;

import javax.annotation.PreDestroy;

import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Profile;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.messaging.rsocket.RSocketRequester.Builder;
import org.springframework.messaging.rsocket.RSocketStrategies;
import org.springframework.messaging.rsocket.annotation.support.RSocketMessageHandler;
import org.springframework.security.rsocket.metadata.SimpleAuthenticationEncoder;
import org.springframework.security.rsocket.metadata.UsernamePasswordMetadata;
import org.springframework.stereotype.Component;
import org.springframework.util.MimeTypeUtils;

import de.freese.spring.rsocket.client.data.MessageRequest;
import de.freese.spring.rsocket.client.data.MessageResponse;
import io.rsocket.SocketAcceptor;
import io.rsocket.frame.decoder.PayloadDecoder;
import io.rsocket.metadata.WellKnownMimeType;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @author Thomas Freese
 */
@Component
@Profile("!test")
public class RSocketClient
{
    /**
     * @author Thomas Freese
     */
    private static class ClientHandler
    {
        /**
         * @param status String
         *
         * @return {@link Publisher}
         */
        @MessageMapping("client-status")
        public Publisher<Long> statusUpdate(final String status)
        {
            LOGGER.info("Connection {}", status);

            // return Flux.interval(Duration.ofSeconds(3)).map(index -> String.valueOf(Runtime.getRuntime().freeMemory()));
            return Mono.delay(Duration.ofSeconds(3)).map(index -> Runtime.getRuntime().freeMemory());
        }
    }

    /**
     *
     */
    private static Disposable disposable;
    /**
     *
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(RSocketClient.class);
    /**
     *
     */
    private RSocketRequester rsocketRequester;
    /**
     *
     */
    private final RSocketRequester.Builder rsocketRequesterBuilder;
    /**
     *
     */
    private final RSocketStrategies rsocketStrategies;

    /**
     * Erstellt ein neues {@link RSocketClient} Object.
     *
     * @param builder {@link Builder}
     * @param strategies {@link RSocketStrategies}
     */
    public RSocketClient(final RSocketRequester.Builder builder, @Qualifier("rSocketStrategies") final RSocketStrategies strategies)
    {
        super();

        this.rsocketRequesterBuilder = Objects.requireNonNull(builder, "builder required");
        this.rsocketStrategies = Objects.requireNonNull(strategies, "strategies required");
    }

    /**
     *
     */
    public void channel()
    {
        if (userIsLoggedIn())
        {
            LOGGER.info("\n\n***** Channel (bi-directional streams)\n***** Asking for a stream of messages.\n***** Type 's' to stop.\n\n");

            Mono<Duration> setting1 = Mono.just(Duration.ofSeconds(1));
            Mono<Duration> setting2 = Mono.just(Duration.ofSeconds(3)).delayElement(Duration.ofSeconds(5));
            Mono<Duration> setting3 = Mono.just(Duration.ofSeconds(5)).delayElement(Duration.ofSeconds(15));

            Flux<Duration> settings =
                    Flux.concat(setting1, setting2, setting3).doOnNext(d -> LOGGER.info("\nSending setting for a {}-second interval.\n", d.getSeconds()));

            //@formatter:off
            disposable = this.rsocketRequester
                    .route("channel")
                    .data(settings)
                    .retrieveFlux(MessageResponse.class)
                    .subscribe(response -> LOGGER.info("Received: {} \n(Type 's' to stop.)", response))
                    ;
            //@formatter:on
        }
    }

    /**
    *
    */
    public void error()
    {
        if (userIsLoggedIn())
        {
            LOGGER.info("Error: retrieve an Error...");

        // @formatter:off
        this.rsocketRequester
               .route("error")
               .data(Mono.empty())
               .retrieveMono(MessageResponse.class)
               .subscribe(response -> LOGGER.info("\nResponse was: {}", response))
               ;
        // @formatter:on
        }
    }

    /**
     * @throws InterruptedException Falls was schief geht.
     */
    public void fireAndForget() throws InterruptedException
    {
        if (userIsLoggedIn())
        {
            LOGGER.info("\nFire-And-Forget. Sending one request. Expect no response (check server console log)...");

            // @formatter:off
            this.rsocketRequester
                .route("fire-and-forget")
                .data(new MessageRequest("me"))
                .send()
                .block()
                ;
            // @formatter:on
        }
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

        LOGGER.info("Connecting using client ID: {} and username: {}", clientId, username);

        SocketAcceptor responder = RSocketMessageHandler.responder(this.rsocketStrategies, new ClientHandler());

        UsernamePasswordMetadata user = new UsernamePasswordMetadata(username, password);

        // @formatter:off
        this.rsocketRequester = this.rsocketRequesterBuilder
                .setupRoute("client-connect")
                .setupData(clientId)
                .setupMetadata(user, MimeTypeUtils.parseMimeType(WellKnownMimeType.MESSAGE_RSOCKET_AUTHENTICATION.getString()))
                .dataMimeType(MimeTypeUtils.APPLICATION_JSON)

                // Wird für Login/Security benötigt.
                .rsocketStrategies(builder ->
                        builder.encoder(new SimpleAuthenticationEncoder()))
                //.rsocketStrategies(this.rsocketStrategies) // Für Verbindung ohne Login/Security.

                .rsocketConnector(connector ->
                    connector
                        .acceptor(responder)
                        .keepAlive(Duration.ofSeconds(30), Duration.ofSeconds(60))
                        .payloadDecoder(PayloadDecoder.ZERO_COPY)
                        .fragment(1492)
                    )
                .tcp("localhost", 7000)
                ;

        this.rsocketRequester.rsocket()
            .onClose()
            .doOnError(error -> LOGGER.warn("Connection CLOSED"))
            .doFinally(consumer -> LOGGER.info("Client DISCONNECTED"))
            .subscribe()
            ;
        // @formatter:on
    }

    /**
     *
     */
    @PreDestroy
    public void logout()
    {
        if (userIsLoggedIn())
        {
            stopStream();
            this.rsocketRequester.rsocket().dispose();
            LOGGER.info("Logged out.");
        }
    }

    /**
     * @throws InterruptedException Falls was schief geht.
     */
    public void parameter() throws InterruptedException
    {
        if (userIsLoggedIn())
        {
            LOGGER.info("Parameter: Sending one request, waiting for one response...");

            //@formatter:off
            this.rsocketRequester
                .route("parameter/me")
                .retrieveMono(MessageResponse.class)
                .subscribe(response -> LOGGER.info("\nResponse was: {}", response))
                ;
            //@formatter:on
        }
    }

    /**
     * @throws InterruptedException Falls was schief geht.
     */
    public void requestResponse() throws InterruptedException
    {
        if (userIsLoggedIn())
        {
            LOGGER.info("\nSending one request. Waiting for one response...");

            // @formatter:off
            this.rsocketRequester
                .route("request-response")
                .data(new MessageRequest("me"))
                .retrieveMono(MessageResponse.class)
                .subscribe(response -> LOGGER.info("\nResponse was: {}", response))
                ;
            // @formatter:on
        }
    }

    /**
     *
     */
    public void startStream()
    {
        if (userIsLoggedIn())
        {
            LOGGER.info("\n\n**** Request-Stream\n**** Send one request.\n**** Log responses.\n**** Type 's' to stop.");

            //@formatter:off
            disposable = this.rsocketRequester
                    .route("stream")
                    .data(new MessageRequest("me"))
                    .retrieveFlux(MessageResponse.class)
                    .subscribe(response -> LOGGER.info("Response: {} \n(Type 's' to stop.)", response))
                    ;
            //@formatter:on
        }
    }

    /**
     *
     */
    public void stopStream()
    {
        if (userIsLoggedIn() && (disposable != null))
        {
            LOGGER.info("Stopping the current stream.");
            disposable.dispose();
            LOGGER.info("Stream stopped.");
        }
    }

    /**
     * @return boolean
     */
    private boolean userIsLoggedIn()
    {
        if ((this.rsocketRequester == null) || this.rsocketRequester.rsocket().isDisposed())
        {
            LOGGER.info("No connection. Did you login?");

            return false;
        }

        return true;
    }
}
