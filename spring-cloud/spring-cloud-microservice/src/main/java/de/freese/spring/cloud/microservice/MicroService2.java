package de.freese.spring.cloud.microservice;

import org.springframework.boot.builder.SpringApplicationBuilder;

/**
 * @author Thomas Freese
 * @since 14.02.2017
 */
public final class MicroService2 {
    static void main() {
        new SpringApplicationBuilder(MicroServiceApplication.class)
                //.properties("server.port=8082") // Doesn't work if server.port configured in application.yml.
                //.run(args);
                .run("--server.port=8082");
    }

    private MicroService2() {
        super();
    }
}
