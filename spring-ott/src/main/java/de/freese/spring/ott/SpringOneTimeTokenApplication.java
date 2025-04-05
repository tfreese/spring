// Created: 05.04.2025
package de.freese.spring.ott;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * <a href="http://localhost:8080">localhost</a><br>
 * Use the generated Link: <a href="http://localhost:8080/login/ott?token=a1760fcc-1460-4282-bd4a-73584b9f13d3">generated</a>
 *
 * @author Thomas Freese
 */
@SpringBootApplication
public final class SpringOneTimeTokenApplication {
    private static final Logger LOGGER = LoggerFactory.getLogger(SpringOneTimeTokenApplication.class);

    public static void main(final String[] args) {
        SpringApplication.run(SpringOneTimeTokenApplication.class, args);

        LOGGER.info("Application started successfully.");
    }
}
