package de.freese.kubernetes.microservice;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Optional;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

/**
 * Hello world!
 */
@SpringBootApplication
public class MyApplication
{
    /**
     * @author Thomas Freese
     */
    @RestController
    class MyRestController
    {
        /**
         * Erstellt ein neues {@link MyRestController} Object.
         */
        public MyRestController()
        {
            super();
        }

        /**
         * @param name {@link Optional}
         * @return {@link Publisher}
         */
        @GetMapping("greet")
        Publisher<String> greet(@RequestParam final Optional<String> name)
        {
            //@formatter:off
            Mono<String> result = name
                    .map(s -> {
                        var msg = "Hello " + s + " on " + getHost();
                        return Mono.just(msg);
                        })
                    //.orElse(Mono.error(new NullPointerException("name")))
                    .orElse(Mono.just("Hello World on " +  getHost()))
                    ;
            //@formatter:on

            return result;
        }
    }

    /**
    *
    */
    private static final Logger LOGGER = LoggerFactory.getLogger(MyApplication.class);

    /**
     * @return String
     */
    private static String getHost()
    {
        try
        {
            return InetAddress.getLocalHost().toString();
        }
        catch (UnknownHostException ex)
        {
            LOGGER.error(null, ex);
        }

        return "???";
    }

    /**
     * @param args String[]
     */
    @SuppressWarnings("resource")
    public static void main(final String[] args)
    {
        SpringApplication.run(MyApplication.class, args);
    }
}
