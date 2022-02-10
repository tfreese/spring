// Created: 11.03.2020
package de.freese.spring.rsocket.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * https://github.com/benwilcock/spring-rsocket-demo
 *
 * @author Thomas Freese
 */
@SpringBootApplication
public class RsocketServerApplication
{
    /**
     * @param args String[]
     */
    public static void main(final String[] args)
    {
        SpringApplication.run(RsocketServerApplication.class, args);
    }
}
