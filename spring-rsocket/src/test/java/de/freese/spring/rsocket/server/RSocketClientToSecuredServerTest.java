package de.freese.spring.rsocket.server;

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
import de.freese.spring.rsocket.GreetingRequest;
import io.rsocket.SocketAcceptor;
import io.rsocket.metadata.WellKnownMimeType;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

/**
 * @author Thomas Freese
 */
@SpringBootTest(classes =
{
        RsocketServerApplication.class
}, properties =
{
        "spring.shell.interactive.enabled=false"
})
@ActiveProfiles(
{
        "test", "server"
})
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
        public Publisher<String> statusUpdate(final String status)
        {
            LOGGER.info("Connection {}", status);

            return Flux.interval(Duration.ofSeconds(5)).map(index -> String.valueOf(Runtime.getRuntime().freeMemory()));
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

        // ******* The user 'test' is NOT in the required 'USER' role! **********
        credentials = new UsernamePasswordMetadata("test", "pass");

        // @formatter:off
        requester = builder
                .setupRoute("client-connect")
                .setupData(UUID.randomUUID().toString())
                .setupMetadata(credentials, mimeType)
                .rsocketStrategies(b -> b.encoder(new SimpleAuthenticationEncoder()))
                .rsocketConnector(connector -> connector.acceptor(responder))
                .connectTcp("localhost", port)
                .block();
        // @formatter:on
    }

    /**
     *
     */
    @AfterAll
    public static void tearDownOnce()
    {
        requester.rsocket().dispose();
    }

    /**
     *
     */
    @Test
    public void testFireAndForget()
    {
        // Send a fire-and-forget message
        // @formatter:off
        Mono<Void> result = requester
                .route("fire-and-forget")
                .data(new GreetingRequest("TEST"))
                .retrieveMono(Void.class);

        // Assert that the user 'test' is DENIED access to the method.
        StepVerifier
                .create(result)
                .verifyErrorMessage("Denied");
        // @formatter:on
    }
}