package de.spring.jooq;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author Thomas Freese
 * @since 25.02.2026
 */
@SpringBootApplication
public final class JooqApplication {
    static void main() {
        SpringApplication.run(JooqApplication.class);
    }

    private JooqApplication() {
        super();
    }
}
