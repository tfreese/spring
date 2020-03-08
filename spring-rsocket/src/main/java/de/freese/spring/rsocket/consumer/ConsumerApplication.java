/**
 * Created: 21.04.2019
 */

package de.freese.spring.rsocket.consumer;

import java.time.Duration;
import java.util.Objects;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.messaging.rsocket.RSocketStrategies;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import de.freese.spring.rsocket.GreetingRequest;
import de.freese.spring.rsocket.GreetingResponse;
import io.rsocket.frame.decoder.PayloadDecoder;
import reactor.core.publisher.Mono;

/**
 * https://spring.io/blog/2019/04/15/spring-tips-rsocket-messaging-in-spring-boot-2-2<br>
 * curl localhost:8080/greet/tommy<br>
 * curl localhost:8080/greet/stream/tommy<br>
 * curl localhost:8080/greet/error<br>
 *
 * @author Thomas Freese
 */
@SpringBootApplication(scanBasePackages = "de.freese.spring.rsocket.consumer")
public class ConsumerApplication
{
    /**
     * @author Thomas Freese
     */
    @RestController
    @RequestMapping(path = "/greet")
    static class GreetingRestController
    {
        /**
         *
         */
        private final RSocketRequester requester;

        /**
         * Erstellt ein neues {@link GreetingRestController} Object.
         *
         * @param requester {@link RSocketRequester}
         */
        GreetingRestController(final RSocketRequester requester)
        {
            super();

            this.requester = Objects.requireNonNull(requester, "requester required");
        }

        /**
         * @return {@link Publisher}
         */
        @GetMapping("/error")
        Publisher<GreetingResponse> error()
        {
            // @formatter:off
            return this.requester
                    .route("error")
                    .data(Mono.empty())
                    .retrieveFlux(GreetingResponse.class);
            // @formatter:on
        }

        /**
         * @param name String
         * @return {@link Publisher}
         */
        @GetMapping("/{name}")
        Publisher<GreetingResponse> greet(@PathVariable final String name)
        {
            // @formatter:off
            return this.requester
                    .route("greet")
                    .data(new GreetingRequest(name))
                    .retrieveMono(GreetingResponse.class);
            // @formatter:on
        }

        /**
         * @param name String
         * @return {@link Publisher}
         */
        @GetMapping(value = "/stream/{name}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
        Publisher<GreetingResponse> stream(@PathVariable final String name)
        {
            // @formatter:off
            return this.requester
                    .route("greet-stream")
                    .data(new GreetingRequest(name))
                    .retrieveFlux(GreetingResponse.class);
            // @formatter:on
        }
    }

    /**
     * @param args String[]
     */
    @SuppressWarnings("resource")
    public static void main(final String[] args)
    {
        // SpringApplication.run(ConsumerApplication.class, args);

        // @formatter:off
        new SpringApplicationBuilder(ConsumerApplication.class)
            .profiles("consumer")
            .run(args)
            ;
        // @formatter:on
    }

    /**
     * Erstellt ein neues {@link ConsumerApplication} Object.
     */
    ConsumerApplication()
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
    RSocketRequester rSocketRequester(final RSocketStrategies rSocketStrategies, @Value("${rsocket.server.address}") final String serverAddress,
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

    // /**
    // * @return {@link RSocket}
    // */
    // @Bean
    // RSocket rSocket()
    // {
//        // @formatter:off
//        return RSocketFactory
//                .connect()
//                .dataMimeType(MimeTypeUtils.APPLICATION_JSON_VALUE, MimeTypeUtils.APPLICATION_JSON_VALUE)
//                .frameDecoder(PayloadDecoder.ZERO_COPY)
//                .transport(TcpClientTransport.create("localhost", 7000))
//                .start()
//                .block();
//        // @formatter:on
    // }
    //
    // /**
    // * @param rSocket {@link RSocket}
    // * @param rSocketStrategies {@link RSocketStrategies}
    // * @return {@link RSocketRequester}
    // */
    // @Bean
    // RSocketRequester rSocketRequester(final RSocket rSocket, final RSocketStrategies rSocketStrategies)
    // {
    // return RSocketRequester.wrap(rSocket, MimeTypeUtils.APPLICATION_JSON, MimeTypeUtils.APPLICATION_JSON_VALUE, rSocketStrategies);
    // }
}
