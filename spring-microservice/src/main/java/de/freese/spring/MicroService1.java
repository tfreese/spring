// Created: 14.02.2017
package de.freese.spring;

import org.springframework.boot.builder.SpringApplicationBuilder;

/**
 * Startklasse des Servers.<br>
 *
 * @author Thomas Freese
 */
public class MicroService1
{
    /**
     * @param args String[]
     */
    public static void main(final String[] args)
    {
        // @formatter:off
        new SpringApplicationBuilder(MicroServiceApplication.class)
                //.properties("server.port=8081") // Funktioniert nicht, wenn server.port in application.yml enthalten ist.
                //.run(args);
                .run("--server.port=8081");
        // @formatter:on
    }
}
