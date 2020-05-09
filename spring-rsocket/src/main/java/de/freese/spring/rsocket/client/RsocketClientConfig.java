/**
 * Created: 11.03.2020
 */

package de.freese.spring.rsocket.client;

import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.reactive.function.client.WebClientCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.messaging.rsocket.RSocketStrategies;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.rsocket.metadata.UsernamePasswordMetadata;
import org.springframework.util.MimeTypeUtils;
import io.rsocket.frame.decoder.PayloadDecoder;

/**
 * @author Thomas Freese
 */
@Configuration
@EnableWebFluxSecurity
@Profile("client")
public class RsocketClientConfig
{
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
        UsernamePasswordMetadata setupCredentials = new UsernamePasswordMetadata("setup", "secret");

        // @formatter:off
        RSocketRequester rSocketRequester = RSocketRequester.builder()
            .dataMimeType(MimeTypeUtils.APPLICATION_JSON)
            //.metadataMimeType(MimeTypeUtils.APPLICATION_JSON) // Verursacht Fehler "No handler for destination"
            //.metadataMimeType(MimeTypeUtils.parseMimeType(WellKnownMimeType.MESSAGE_RSOCKET_COMPOSITE_METADATA.getString())) // Default
            .rsocketStrategies(rSocketStrategies)
            .rsocketConnector(connector -> {
                connector
                    .keepAlive(Duration.ofSeconds(60), Duration.ofSeconds(30))
                    .payloadDecoder(PayloadDecoder.ZERO_COPY)
                    .fragment(1492)
                ;
            })
            .setupMetadata(setupCredentials, UsernamePasswordMetadata.BASIC_AUTHENTICATION_MIME_TYPE)
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
