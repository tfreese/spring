/**
 * Created: 11.03.2020
 */

package de.freese.spring.rsocket.client;

import java.time.Duration;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.reactive.function.client.WebClientCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.messaging.rsocket.RSocketStrategies;
import org.springframework.messaging.rsocket.annotation.support.RSocketMessageHandler;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.rsocket.metadata.UsernamePasswordMetadata;
import org.springframework.util.MimeTypeUtils;
import io.rsocket.SocketAcceptor;
import io.rsocket.frame.decoder.PayloadDecoder;
import reactor.core.publisher.Flux;

/**
 * @author Thomas Freese
 */
@Configuration
@EnableWebFluxSecurity
@Profile("client")
public class RsocketClientConfig
{
    /**
     * @author Thomas Freese
     */
    private static class ClientHandler
    {
        /**
         * @param status String
         * @return {@link Flux}
         */
        @MessageMapping("client-status")
        public Flux<String> statusUpdate(final String status)
        {
            LOGGER.info("Connection {}", status);

            // return Mono.just(System.getProperty("java.vendor") + " v" + System.getProperty("java.version"));

            return Flux.interval(Duration.ofSeconds(5)).map(index -> String.valueOf(Runtime.getRuntime().freeMemory()));
        }
    }

    /**
    *
    */
    private static final Logger LOGGER = LoggerFactory.getLogger(RsocketClientConfig.class);

    /**
     * Erstellt ein neues {@link RsocketClientConfig} Object.
     */
    public RsocketClientConfig()
    {
        super();
    }

    /**
     * @param rSocketStrategies {@link RSocketStrategies}
     * @param serverAddress String
     * @param serverPort int
     * @return {@link RSocketRequester}
     */
    @Bean
    public RSocketRequester rSocketRequester(final RSocketStrategies rSocketStrategies, @Value("${rsocket.server.address}") final String serverAddress,
                                             @Value("${rsocket.server.port}") final int serverPort)
    {
        String client = UUID.randomUUID().toString();
        LOGGER.info("Connecting using client ID: {}", client);

        UsernamePasswordMetadata setupCredentials = new UsernamePasswordMetadata("setup", "secret");

        SocketAcceptor responder = RSocketMessageHandler.responder(rSocketStrategies, new ClientHandler());

        // @formatter:off
        RSocketRequester rSocketRequester = RSocketRequester.builder()
                .setupRoute("client-connect") // -> RSocketController#connectClientAndAskForTelemetry
                .setupData(client)
                .setupMetadata(setupCredentials, RsocketClientApplication.BASIC_AUTHENTICATION_MIME_TYPE)
                .dataMimeType(MimeTypeUtils.APPLICATION_JSON)
                //.metadataMimeType(MimeTypeUtils.APPLICATION_JSON) // Verursacht Fehler "No handler for destination"
                //.metadataMimeType(MimeTypeUtils.parseMimeType(WellKnownMimeType.MESSAGE_RSOCKET_COMPOSITE_METADATA.getString())) // Default
                .rsocketStrategies(rSocketStrategies)
                .rsocketConnector(connector -> {
                    connector
                        .acceptor(responder)
                        .keepAlive(Duration.ofSeconds(30), Duration.ofSeconds(60))
                        .payloadDecoder(PayloadDecoder.ZERO_COPY)
                        .fragment(1492)
                        ;
                })
                .connectTcp(serverAddress, serverPort)
                .block()
                ;
        // @formatter:on

        return rSocketRequester;
    }

    /**
     * @param serverAddress String
     * @param serverPort int
     * @return {@link WebClientCustomizer}
     */
    @Bean
    public WebClientCustomizer webClientCustomizer(@Value("${server.address}") final String serverAddress, @Value("${server.port}") final int serverPort)
    {
        return webClientBuilder -> {
            webClientBuilder.baseUrl("http://" + serverAddress + ":" + serverPort);
        };
    }
}
