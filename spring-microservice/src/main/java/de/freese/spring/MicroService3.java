// Created: 14.02.2017
package de.freese.spring;

import org.springframework.boot.builder.SpringApplicationBuilder;

/**
 * Startklasse des Servers.<br>
 *
 * @author Thomas Freese
 */
public class MicroService3
{
    /**
     * @param args String[]
     */
    @SuppressWarnings("resource")
    public static void main(final String[] args)
    {
        // @formatter:off
        new SpringApplicationBuilder(MicroServiceApplication.class)
                //.properties("server.port=8083") // Funktioniert nicht, wenn server.port in application.yml enthalten ist.
                //.run(args);
                .run(new String[]{"--server.port=8083"});
        // @formatter:on
    }
}
