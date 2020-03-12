/**
 * Created: 11.03.2020
 */

package de.freese.spring.rsocket.client;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.web.reactive.config.EnableWebFlux;

/**
 * @author Thomas Freese
 */
@SpringBootApplication(scanBasePackages = "de.freese.spring.rsocket.client")
@EnableWebFlux
public class RsocketClientApplication
{
    /**
     * @param args String[]
     */
    @SuppressWarnings("resource")
    public static void main(final String[] args)
    {
        // SpringApplication.run(RsocketClientApplication.class, args);

        // @formatter:off
        new SpringApplicationBuilder(RsocketClientApplication.class)
            .profiles("client")
            .properties("spring.shell.interactive.enabled=true")
            .run(args)
            ;
        // @formatter:on
    }
}
