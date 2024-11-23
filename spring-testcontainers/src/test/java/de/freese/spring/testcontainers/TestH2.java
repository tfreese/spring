package de.freese.spring.testcontainers;

import java.util.UUID;

import org.springframework.boot.jdbc.DatabaseDriver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistrar;

/**
 * @author Thomas Freese
 */
//@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD) // Jede Methode mit eigenem Context.
@ActiveProfiles("test-h2")
class TestH2 extends AbstractTest {
    @Configuration
    @Profile("test-h2")
    static class TestConfig {
        @Bean
        DynamicPropertyRegistrar dynamicPropertyRegistrar() {
            return registry -> {
                final String id = UUID.randomUUID().toString();

                registry.add("spring.datasource.driver-class-name", DatabaseDriver.H2::getDriverClassName);
                registry.add("spring.datasource.url", () -> "jdbc:h2:mem:" + UUID.randomUUID() + ";DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=false");
                registry.add("spring.datasource.username", () -> "sa");
                registry.add("spring.datasource.password", () -> "");

                registry.add("spring.datasource.hikari.pool-name", () -> "Hikari-H2-" + id);
            };
        }
    }

    // @DynamicPropertySource
    // static void properties(final DynamicPropertyRegistry registry) {
    //     final String id = UUID.randomUUID().toString();
    //
    //     registry.add("spring.datasource.driver-class-name", DatabaseDriver.H2::getDriverClassName);
    //     registry.add("spring.datasource.url", () -> "jdbc:h2:mem:" + UUID.randomUUID() + ";DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=false");
    //     registry.add("spring.datasource.username", () -> "sa");
    //     registry.add("spring.datasource.password", () -> "");
    //
    //     registry.add("spring.datasource.hikari.pool-name", () -> "Hikari-H2-" + id);
    // }
}
