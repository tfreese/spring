// Created: 18.06.2020
package de.freese.spring.rsocket;

import java.util.Optional;

import io.rsocket.RSocket;
import io.rsocket.core.RSocketClient;
import io.rsocket.metadata.WellKnownMimeType;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.rsocket.server.LocalRSocketServerPort;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.messaging.rsocket.RSocketStrategies;
import org.springframework.security.rsocket.metadata.UsernamePasswordMetadata;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.util.MimeType;
import org.springframework.util.MimeTypeUtils;
import reactor.core.publisher.Hooks;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import de.freese.spring.rsocket.model.MessageRequest;

/**
 * @author Thomas Freese
 */
@SpringBootTest(properties = "spring.rsocket.server.port = 0", webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles({"test", "simple"})
@Disabled("bleibt hängen")
class RSocketClientToServerDeniedConnectionTest {
    private static RSocketRequester requester;

    @AfterAll
    static void afterAll() {
        Optional.ofNullable(requester.rsocketClient()).ifPresent(RSocketClient::dispose);
        Optional.ofNullable(requester.rsocket()).ifPresent(RSocket::dispose);
    }

    @BeforeAll
    static void beforeAll(@Autowired final RSocketRequester.Builder builder, @Autowired final RSocketStrategies strategies, @Value("${spring.rsocket.server.address}") final String host, @LocalRSocketServerPort final int port) {
        // Fehlermeldung, wenn Client die Verbindung schliesst.
        // Nur einmalig definieren, sonst gib es mehrere Logs-Meldungen !!!
        // Hooks.onErrorDropped(th -> LOGGER.warn(th.getMessage()));
        Hooks.onErrorDropped(th -> {
            // Empty
        });

        final UsernamePasswordMetadata credentials = new UsernamePasswordMetadata("fake", "pass");
        final MimeType mimeType = MimeTypeUtils.parseMimeType(WellKnownMimeType.MESSAGE_RSOCKET_AUTHENTICATION.getString());

        // @formatter:off
        requester = builder
                .setupMetadata(credentials, mimeType)
                .tcp(host, port)
                ;
        // @formatter:on
    }

    @Test
    void testConnectionIsRefused() {
        // @formatter:off
        final Mono<Void> result = requester
                .route("fire-and-forget")
                .data(new MessageRequest("TEST - fire-and-forget"))
                .retrieveMono(Void.class)
                ;

        StepVerifier
                .create(result)
                .verifyErrorMessage("Invalid Credentials")
                ;
        // @formatter:on
    }
}
