/**
 * Created: 11.03.2020
 */

package de.freese.spring.rsocket.client;

import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.messaging.rsocket.RSocketStrategies;
import org.springframework.util.MimeTypeUtils;
import io.rsocket.frame.decoder.PayloadDecoder;

/**
 * @author Thomas Freese
 */
@Configuration
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
        // @formatter:off
        RSocketRequester rSocketRequester = RSocketRequester.builder()
            .rsocketFactory(factory -> factory
                    .keepAlive(Duration.ofSeconds(60), Duration.ofSeconds(30), 3)
                    .frameDecoder(PayloadDecoder.ZERO_COPY)
                )
            .dataMimeType(MimeTypeUtils.APPLICATION_JSON)
            //.metadataMimeType(MimeTypeUtils.APPLICATION_JSON) // Verursacht Fehler "No handler for destination"
            //.metadataMimeType(MimeTypeUtils.parseMimeType(WellKnownMimeType.MESSAGE_RSOCKET_COMPOSITE_METADATA.getString())) // Default
            .rsocketStrategies(rSocketStrategies)
            .connectTcp(serverAddress, serverPort)
            .block()
            ;
        // @formatter:on

        return rSocketRequester;
    }
}
