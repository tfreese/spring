/**
 * Created: 11.03.2020
 */

package de.freese.spring.rsocket.client;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.reactive.ReactiveSecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.reactive.ReactiveUserDetailsServiceAutoConfiguration;
import org.springframework.boot.autoconfigure.security.rsocket.RSocketSecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;

/**
 * @author Thomas Freese
 */
@SpringBootApplication(scanBasePackages = "de.freese.spring.rsocket.client", exclude =
{
        ReactiveUserDetailsServiceAutoConfiguration.class,
        SecurityAutoConfiguration.class,
        ReactiveSecurityAutoConfiguration.class,
        RSocketSecurityAutoConfiguration.class
})
public class RsocketClientApplication
{
    // /**
    // *
    // */
    // public static final MimeType BASIC_AUTHENTICATION_MIME_TYPE = MimeTypeUtils.parseMimeType(WellKnownMimeType.MESSAGE_RSOCKET_AUTHENTICATION.getString());

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
