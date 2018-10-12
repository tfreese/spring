// Created: 14.02.2017
package de.freese.spring;

import org.springframework.boot.builder.SpringApplicationBuilder;

/**
 * Startklasse des Servers.<br>
 *
 * @author Thomas Freese
 */
public class Server1
{
    /**
     * @param args String[]
     */
    public static void main(final String[] args)
    {
        // @formatter:off
        new SpringApplicationBuilder(Server.class)
                //.properties("server.port=8081") // Funktioniert nicht, wenn server.port in application.yml enthalten ist.
                //.run(args);
                .run(new String[]{"--server.port=8081"});
        // @formatter:on
    }

    /**
     * Erzeugt eine neue Instanz von {@link Server1}
     */
    public Server1()
    {
        super();
    }
}
