// Created: 10.06.2015
package de.freese.spring.web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author Thomas Freese
 */
@SpringBootApplication
// @SuppressWarnings("checkstyle:HideUtilityClassConstructor")
public final class SpringBootWebApplication {
    static void main(final String[] args) {
        SpringApplication.run(SpringBootWebApplication.class, args);
    }

    private SpringBootWebApplication() {
        super();
    }
}
