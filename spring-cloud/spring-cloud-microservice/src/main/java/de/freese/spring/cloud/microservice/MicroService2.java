// Created: 14.02.2017
package de.freese.spring.cloud.microservice;

import org.springframework.boot.builder.SpringApplicationBuilder;

/**
 * Startklasse des Servers.<br>
 *
 * @author Thomas Freese
 */
public final class MicroService2 {
    public static void main(final String[] args) {
        new SpringApplicationBuilder(MicroServiceApplication.class)
                //.properties("server.port=8082") // Funktioniert nicht, wenn server.port in application.yml enthalten ist.
                //.run(args);
                .run("--server.port=8082");
    }

    private MicroService2() {
        super();
    }
}
