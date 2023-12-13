package de.freese.spring.testcontainers;

import java.util.UUID;

import org.springframework.boot.jdbc.DatabaseDriver;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

/**
 * @author Thomas Freese
 */
//@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD) // Jede Methode mit eigenem Context.
class TestH2 extends AbstractTest {
    @DynamicPropertySource
    static void properties(final DynamicPropertyRegistry registry) {
        String id = UUID.randomUUID().toString();

        registry.add("spring.datasource.driver-class-name", DatabaseDriver.H2::getDriverClassName);
        registry.add("spring.datasource.url", () -> "jdbc:h2:mem:" + UUID.randomUUID() + ";DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=false");
        registry.add("spring.datasource.username", () -> "sa");
        registry.add("spring.datasource.password", () -> "");

        registry.add("spring.datasource.hikari.pool-name", () -> "Hikari-H2-" + id);
    }
}
