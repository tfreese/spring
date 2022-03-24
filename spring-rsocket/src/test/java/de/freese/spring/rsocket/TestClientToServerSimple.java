package de.freese.spring.rsocket;

import java.util.Optional;

import io.rsocket.RSocket;
import io.rsocket.core.RSocketClient;
import io.rsocket.metadata.WellKnownMimeType;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.rsocket.context.LocalRSocketServerPort;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.messaging.rsocket.RSocketRequester.Builder;
import org.springframework.messaging.rsocket.RSocketStrategies;
import org.springframework.security.rsocket.metadata.UsernamePasswordMetadata;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.util.MimeType;
import org.springframework.util.MimeTypeUtils;
import reactor.core.publisher.Hooks;

/**
 * @author Thomas Freese
 */
@ActiveProfiles(
        {
                "test", "simple"
        })
class TestClientToServerSimple implements TestClientToServer
{
    /**
     *
     */
    private static RSocketRequester requester;

    /**
     *
     */
    @AfterAll
    public static void afterAll()
    {
        Optional.ofNullable(requester.rsocketClient()).ifPresent(RSocketClient::dispose);
        Optional.ofNullable(requester.rsocket()).ifPresent(RSocket::dispose);
    }

    /**
     * @param builder {@link Builder}
     * @param strategies {@link RSocketStrategies}
     * @param host String
     * @param port int
     */
    @BeforeAll
    public static void beforeAll(@Autowired final RSocketRequester.Builder builder, @Autowired final RSocketStrategies strategies,
                                 @Value("${spring.rsocket.server.address}") final String host, @LocalRSocketServerPort final int port)
    {
        // Fehlermeldung, wenn Client die Verbindung schliesst.
        // Nur einmalig definieren, sonst gib es mehrere Logs-Meldungen !!!
        // Hooks.onErrorDropped(th -> LOGGER.warn(th.getMessage()));
        Hooks.onErrorDropped(th ->
        {
            // Empty
        });

        UsernamePasswordMetadata credentials = new UsernamePasswordMetadata("user", "pass");
        MimeType mimeType = MimeTypeUtils.parseMimeType(WellKnownMimeType.MESSAGE_RSOCKET_AUTHENTICATION.getString());

        // @formatter:off
        requester = builder
                .setupMetadata(credentials, mimeType)
                .tcp(host, port)
                ;
        // @formatter:on
    }

    /**
     * @see de.freese.spring.rsocket.TestClientToServer#getRequester()
     */
    @Override
    public RSocketRequester getRequester()
    {
        return requester;
    }
}
