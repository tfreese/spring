/**
 * Created: 21.04.2019
 */

package de.freese.spring.rsocket.producer;

import java.time.Duration;
import java.util.stream.Stream;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;
import de.freese.spring.rsocket.GreetingRequest;
import de.freese.spring.rsocket.GreetingResponse;
import reactor.core.publisher.Flux;

/**
 * https://spring.io/blog/2019/04/15/spring-tips-rsocket-messaging-in-spring-boot-2-2
 *
 * @author Thomas Freese
 */
@SpringBootApplication(scanBasePackages = "de.freese.spring.rsocket.producer")
public class ProducerApplication
{
    /**
     * @author Thomas Freese
     */
    @Controller
    static class GreetingRSocketController
    {
        /**
         * Erstellt ein neues {@link GreetingRSocketController} Object.
         */
        GreetingRSocketController()
        {
            super();
        }

        /**
         * @return {@link GreetingResponse}
         */
        @MessageMapping("error")
        Flux<GreetingResponse> error()
        {
            return Flux.error(new IllegalArgumentException());
        }

        /**
         * @param iaex {@link IllegalArgumentException}
         * @return {@link Flux}
         */
        @MessageExceptionHandler
        Flux<GreetingResponse> errorHandler(final IllegalArgumentException iaex)
        {
            return Flux.just(new GreetingResponse("Oh no !"));
        }

        /**
         * @param request {@link GreetingRequest}
         * @return {@link GreetingResponse}
         */
        @MessageMapping("greet")
        GreetingResponse greet(final GreetingRequest request)
        {
            return new GreetingResponse(request.getName());
        }

        /**
         * @param request {@link GreetingRequest}
         * @return {@link GreetingResponse}
         */
        @MessageMapping("greet-stream")
        Flux<GreetingResponse> greetStream(final GreetingRequest request)
        {
            // @formatter:off
            return Flux.fromStream(Stream.generate(() -> new GreetingResponse(request.getName())))
                    .take(5L)
                    .delayElements(Duration.ofSeconds(1));
            // @formatter:on
        }
    }

    /**
     * @param args String[]
     */
    public static void main(final String[] args)
    {
        // SpringApplication.run(ProducerApplication.class, args);

        // @formatter:off
        new SpringApplicationBuilder(ProducerApplication.class)
            .profiles("producer")
            .run(args);
        // @formatter:on
    }

    /**
     * Erstellt ein neues {@link ProducerApplication} Object.
     */
    ProducerApplication()
    {
        super();
    }
}
