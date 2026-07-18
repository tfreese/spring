// Created: 17.07.2026
package de.spring.ai.simple;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author Thomas Freese
 */
@SpringBootApplication
public final class GithubApplication {
    static void main(final String[] args) {
        SpringApplication.run(GithubApplication.class, args);
    }

    private GithubApplication() {
        super();
    }
}
