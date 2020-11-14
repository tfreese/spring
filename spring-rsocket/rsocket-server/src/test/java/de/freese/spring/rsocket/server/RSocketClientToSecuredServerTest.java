package de.freese.spring.rsocket.server;

import static org.junit.jupiter.api.Assertions.assertEquals;
import java.time.Duration;
import java.util.UUID;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.rsocket.context.LocalRSocketServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.messaging.rsocket.RSocketRequester.Builder;
import org.springframework.messaging.rsocket.RSocketStrategies;
import org.springframework.messaging.rsocket.annotation.support.RSocketMessageHandler;
import org.springframework.security.rsocket.metadata.SimpleAuthenticationEncoder;
import org.springframework.security.rsocket.metadata.UsernamePasswordMetadata;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.util.MimeType;
import org.springframework.util.MimeTypeUtils;
import de.freese.spring.rsocket.server.data.MessageRequest;
import de.freese.spring.rsocket.server.data.MessageResponse;
import io.rsocket.SocketAcceptor;
import io.rsocket.metadata.WellKnownMimeType;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

/**
 * @author Thomas Freese
 */
@SpringBootTest
@ActiveProfiles("test")
class RSocketClientToSecuredServerTest
{

    /**
     * @author Thomas Freese
     */
    static class ClientHandler
    {
        /**
         * @param status String
         * @return {@link Publisher}
         */
        @MessageMapping("client-status")
        public Publisher<Long> statusUpdate(final String status)
        {
            LOGGER.info("Connection {}", status);

            return Flux.interval(Duration.ofSeconds(3)).map(index -> Runtime.getRuntime().freeMemory());
        }
    }

    /**
     *
     */
    private static UsernamePasswordMetadata credentials = null;

    /**
    *
    */
    private static final Logger LOGGER = LoggerFactory.getLogger(RSocketClientToSecuredServerTest.class);

    /**
     *
     */
    private static MimeType mimeType = null;

    /**
     *
     */
    private static RSocketRequester requester = null;

    /**
     * @param builder {@link Builder}
     * @param port int
     * @param strategies {@link RSocketStrategies}
     */
    @BeforeAll
    static void setupOnce(@Autowired final RSocketRequester.Builder builder, @LocalRSocketServerPort final int port,
                          @Autowired final RSocketStrategies strategies)
    {
        SocketAcceptor responder = RSocketMessageHandler.responder(strategies, new ClientHandler());
        mimeType = MimeTypeUtils.parseMimeType(WellKnownMimeType.MESSAGE_RSOCKET_AUTHENTICATION.getString());

        // ******* The user 'fail' is NOT in the required 'USER' role! **********
        credentials = new UsernamePasswordMetadata("fail", "pass");

        // @formatter:off
        requester = builder
                .setupRoute("client-connect")
                .setupData(UUID.randomUUID().toString())
                .setupMetadata(credentials, mimeType)

                // Wird für Login/Security benötigt.
                .rsocketStrategies(b -> b.encoder(new SimpleAuthenticationEncoder()))
                //.rsocketStrategies(this.rsocketStrategies) // Für Verbindung ohne Login/Security.

                .rsocketConnector(connector -> connector.acceptor(responder))
                .tcp("localhost", port)
                ;
        // @formatter:on
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
    void testFireAndForget()
    {
        // Send a fire-and-forget message
        // @formatter:off
        Mono<MessageResponse> result = requester
                .route("fire-and-forget")
                .data(new MessageRequest("TEST - fire-and-forget"))
                //.retrieveMono(Void.class) // Ohne MessageExceptionHandler
                .retrieveMono(MessageResponse.class) // Response enthält die Fehlermeldung, nur mit MessageExceptionHandler.
                ;

        //result.subscribe(System.out::println);

        // Assert that the user 'fail' is DENIED access to the method.
        StepVerifier
                .create(result)
                .consumeNextWith(response -> {
                    assertEquals("AccessDeniedException: Denied", response.getMessage());
                    assertEquals(0, response.getIndex());
                })
                .verifyComplete()
                //.verifyErrorMessage("Denied") // Ohne MessageExceptionHandler
                ;
        // @formatter:on
    }
}