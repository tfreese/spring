// Created: 14.02.2017
package de.freese.spring.cloud.microservice;

import org.springframework.boot.builder.SpringApplicationBuilder;

/**
 * @author Thomas Freese
 */
public final class MicroService1 {
    static void main() {
        new SpringApplicationBuilder(MicroServiceApplication.class)
                //.properties("server.port=8081") // Doesn't work if server.port configured in application.yml.
                //.run(args);
                .run("--server.port=8081");
    }

    private MicroService1() {
        super();
    }
}
