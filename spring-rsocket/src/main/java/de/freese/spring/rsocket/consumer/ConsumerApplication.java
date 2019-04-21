/**
 * Created: 21.04.2019
 */

package de.freese.spring.rsocket.consumer;

import java.util.Objects;
import org.reactivestreams.Publisher;
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
import io.rsocket.RSocket;
import io.rsocket.RSocketFactory;
import io.rsocket.frame.decoder.PayloadDecoder;
import io.rsocket.transport.netty.client.TcpClientTransport;
import reactor.core.publisher.Mono;

/**
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
    public static void main(final String[] args)
    {
        // SpringApplication.run(ConsumerApplication.class, args);
        new SpringApplicationBuilder(ConsumerApplication.class).profiles("consumer").run(args);
    }

    /**
     * Erstellt ein neues {@link ConsumerApplication} Object.
     */
    ConsumerApplication()
    {
        super();
    }

    /**
     * @return {@link RSocket}
     */
    @Bean
    RSocket rSocket()
    {
        // @formatter:off
        return RSocketFactory
                .connect()
                .dataMimeType(MimeTypeUtils.APPLICATION_JSON_VALUE)
                .frameDecoder(PayloadDecoder.ZERO_COPY)
                .transport(TcpClientTransport.create(7000))
                .start()
                .block();
        // @formatter:on
    }

    /**
     * @param rSocket {@link RSocket}
     * @param rSocketStrategies {@link RSocketStrategies}
     * @return {@link RSocketRequester}
     */
    @Bean
    RSocketRequester rSocketRequester(final RSocket rSocket, final RSocketStrategies rSocketStrategies)
    {
        return RSocketRequester.create(rSocket, MimeTypeUtils.APPLICATION_JSON, rSocketStrategies);
    }
}
