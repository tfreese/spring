/**
 * Created: 11.03.2020
 */

package de.freese.spring.rsocket.server;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

/**
 * @author Thomas Freese
 */
@SpringBootApplication(scanBasePackages = "de.freese.spring.rsocket.server")
public class RsocketServerApplication
{
    /**
     * @param args String[]
     */
    @SuppressWarnings("resource")
    public static void main(final String[] args)
    {
        // SpringApplication.run(RsocketServerApplication.class, args);

        // @formatter:off
        new SpringApplicationBuilder(RsocketServerApplication.class)
            .profiles("server")
            .properties("spring.shell.interactive.enabled=false")
            .run(args)
            ;
        // @formatter:on
    }
}
