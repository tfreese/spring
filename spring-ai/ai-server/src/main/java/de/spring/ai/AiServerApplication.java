package de.spring.ai;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author Thomas Freese
 */
@SpringBootApplication
public final class AiServerApplication {
    static void main(final String[] args) {
        SpringApplication.run(AiServerApplication.class, args);
    }

    private AiServerApplication() {
        super();
    }
}
