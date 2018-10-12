// Created: 14.02.2017
package de.freese.spring;

import org.springframework.boot.builder.SpringApplicationBuilder;

/**
 * Startklasse des Servers.<br>
 *
 * @author Thomas Freese
 */
public class Server2
{
    /**
     * @param args String[]
     */
    public static void main(final String[] args)
    {
        // @formatter:off
        new SpringApplicationBuilder(Server.class)
                //.properties("server.port=8082") // Funktioniert nicht, wenn server.port in application.yml enthalten ist.
                //.run(args);
                .run(new String[]{"--server.port=8082"});
        // @formatter:on
    }

    /**
     * Erzeugt eine neue Instanz von {@link Server2}
     */
    public Server2()
    {
        super();
    }
}
