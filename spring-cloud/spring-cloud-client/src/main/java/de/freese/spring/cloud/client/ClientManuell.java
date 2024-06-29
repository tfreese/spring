// Created: 14.02.2017
package de.freese.spring.cloud.client;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

/**
 * @author Thomas Freese
 */
@SpringBootApplication
@SuppressWarnings("checkstyle:HideUtilityClassConstructor")
public class ClientManuell {
    public static void main(final String[] args) {
        new SpringApplicationBuilder(ClientManuell.class).run("--spring.profiles.active=manuell");
    }
}
