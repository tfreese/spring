// Created: 14.02.2017
package de.freese.spring.cloud.client;

import org.springframework.boot.builder.SpringApplicationBuilder;

/**
 * Startklasse des Servers.<br>
 *
 * @author Thomas Freese
 */
public class ClientEureka
{
    /**
     * @param args String[]
     */
    public static void main(final String[] args)
    {
        // @formatter:off
        new SpringApplicationBuilder(ClientApplication.class)
                .run("--spring.profiles.active=eureka");
        // @formatter:on
    }
}
