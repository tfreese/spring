package de.freese.spring.boot.cloud.admin;

import de.codecentric.boot.admin.server.config.EnableAdminServer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author Thomas Freese
 * @since 10.10.2017
 */
@SpringBootApplication
@EnableAdminServer
@SuppressWarnings("checkstyle:HideUtilityClassConstructor")
public final class SpringBootAdminApplication {
    static void main(final String[] args) {
        SpringApplication.run(SpringBootAdminApplication.class, args);
    }

    private SpringBootAdminApplication() {
        super();
    }
}
