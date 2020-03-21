// Created: 14.02.2017
package de.freese.spring;

import org.springframework.boot.builder.SpringApplicationBuilder;

/**
 * Startklasse des Servers.<br>
 *
 * @author Thomas Freese
 */
public class MicroService2
{
    /**
     * @param args String[]
     */
    @SuppressWarnings("resource")
    public static void main(final String[] args)
    {
        // @formatter:off
        new SpringApplicationBuilder(MicroServiceApplication.class)
                //.properties("server.port=8082") // Funktioniert nicht, wenn server.port in application.yml enthalten ist.
                //.run(args);
                .run(new String[]{"--server.port=8082"});
        // @formatter:on
    }

    /**
     * Erzeugt eine neue Instanz von {@link MicroService2}
     */
    private MicroService2()
    {
        super();
    }
}
