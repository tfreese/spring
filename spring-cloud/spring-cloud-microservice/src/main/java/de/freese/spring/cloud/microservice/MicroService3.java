// Created: 14.02.2017
package de.freese.spring.cloud.microservice;

import org.springframework.boot.builder.SpringApplicationBuilder;

/**
 * @author Thomas Freese
 */
public final class MicroService3 {
    public static void main(final String[] args) {
        new SpringApplicationBuilder(MicroServiceApplication.class)
                //.properties("server.port=8083") // Doesn't work if server.port configured in application.yml.
                //.run(args);
                .run("--server.port=8083");
    }

    private MicroService3() {
        super();
    }
}
