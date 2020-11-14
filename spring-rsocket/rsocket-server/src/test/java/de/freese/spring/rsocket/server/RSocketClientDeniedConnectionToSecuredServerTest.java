/**
 * Created: 18.06.2020
 */

package de.freese.spring.rsocket.server;

import java.util.UUID;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.rsocket.context.LocalRSocketServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.messaging.rsocket.RSocketRequester.Builder;
import org.springframework.messaging.rsocket.RSocketStrategies;
import org.springframework.security.rsocket.metadata.SimpleAuthenticationEncoder;
import org.springframework.security.rsocket.metadata.UsernamePasswordMetadata;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.util.MimeType;
import org.springframework.util.MimeTypeUtils;
import de.freese.spring.rsocket.server.data.MessageRequest;
import io.rsocket.metadata.WellKnownMimeType;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

/**
 * @author Thomas Freese
 */
@SpringBootTest
@ActiveProfiles("test")
class RSocketClientDeniedConnectionToSecuredServerTest
{
    /**
     *
     */
    private static UsernamePasswordMetadata credentials = null;

    /**
     *
     */
    private static MimeType mimeType = null;

    /**
     *
     */
    private static RSocketRequester.Builder reqbuilder = null;

    /**
     *
     */
    private static RSocketRequester requester = null;

    /**
     *
     */
    private static int thePort;

    /**
     * @param builder {@link Builder}
     * @param port int; @LocalRSocketServerPort; @Value("${spring.rsocket.server.port}")
     * @param strategies {@link RSocketStrategies}
     */
    @BeforeAll
    static void setupOnce(@Autowired final RSocketRequester.Builder builder, @LocalRSocketServerPort final int port,
                          @Autowired final RSocketStrategies strategies)
    {
        mimeType = MimeTypeUtils.parseMimeType(WellKnownMimeType.MESSAGE_RSOCKET_AUTHENTICATION.getString());
        reqbuilder = builder;
        thePort = port;

        // ******* The user 'fake' is NOT in the user list! **********
        credentials = new UsernamePasswordMetadata("fake", "pass");
    }

    /**
     *
     */
    @AfterAll
    static void tearDownOnce()
    {
        requester.rsocket().dispose();
    }

    /**
     *
     */
    @Test
    void testConnectionIsRefused()
    {
        // @formatter:off
        requester = reqbuilder
                .setupRoute("client-connect")
                .setupData(UUID.randomUUID().toString())
                .setupMetadata(credentials, mimeType)

                // Wird für Login/Security benötigt.
                .rsocketStrategies(builder -> builder.encoder(new SimpleAuthenticationEncoder()))
                .tcp("localhost", thePort)
                ;

        Mono<Void> result = requester
                .route("fire-and-forget")
                .data(new MessageRequest("TEST - fire-and-forget"))
                .retrieveMono(Void.class);

        StepVerifier
                .create(result)
                .verifyErrorMessage("Invalid Credentials");
        // @formatter:on
    }
}
