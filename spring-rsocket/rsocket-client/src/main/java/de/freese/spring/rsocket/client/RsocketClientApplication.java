// Created: 11.03.2020
package de.freese.spring.rsocket.client;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.reactive.ReactiveSecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.reactive.ReactiveUserDetailsServiceAutoConfiguration;
import org.springframework.boot.autoconfigure.security.rsocket.RSocketSecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;

/**
 * https://github.com/benwilcock/spring-rsocket-demo
 *
 * @author Thomas Freese
 */
@SpringBootApplication(exclude =
{
        ReactiveUserDetailsServiceAutoConfiguration.class,
        SecurityAutoConfiguration.class,
        ReactiveSecurityAutoConfiguration.class,
        RSocketSecurityAutoConfiguration.class
})
public class RsocketClientApplication
{
    /**
     * @param args String[]
     */
    public static void main(final String[] args)
    {
        SpringApplication.run(RsocketClientApplication.class, args);
    }
}
