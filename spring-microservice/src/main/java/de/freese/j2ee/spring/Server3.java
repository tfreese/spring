// Created: 14.02.2017
package de.freese.j2ee.spring;

import org.springframework.boot.builder.SpringApplicationBuilder;

/**
 * Startklasse des Servers.<br>
 *
 * @author Thomas Freese
 */
public class Server3
{
    /**
     * @param args String[]
     */
    public static void main(final String[] args)
    {
        // @formatter:off
        new SpringApplicationBuilder(Server.class)
                //.properties("server.port=8083") // Funktioniert nicht, wenn server.port in application.yml enthalten ist.
                //.run(args);
                .run(new String[]{"--server.port=8083"});;
        // @formatter:on
    }

    /**
     * Erzeugt eine neue Instanz von {@link Server3}
     */
    public Server3()
    {
        super();
    }
}
