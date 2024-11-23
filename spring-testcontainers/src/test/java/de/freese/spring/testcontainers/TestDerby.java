package de.freese.spring.testcontainers;

import java.util.UUID;

import org.junit.jupiter.api.Disabled;
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
@Disabled("Derby löscht die Sequence nicht")
@ActiveProfiles("test-derby")
class TestDerby extends AbstractTest {
    @Configuration
    @Profile("test-derby")
    static class TestConfig {
        @Bean
        DynamicPropertyRegistrar dynamicPropertyRegistrar() {
            return registry -> {
                final String id = UUID.randomUUID().toString();

                registry.add("spring.datasource.driver-class-name", DatabaseDriver.DERBY::getDriverClassName);
                registry.add("spring.datasource.url", () -> "jdbc:derby:memory:" + id + ";create=true");
                registry.add("spring.datasource.username", () -> "sa");
                registry.add("spring.datasource.password", () -> "");

                registry.add("spring.datasource.hikari.pool-name", () -> "Hikari-Derby-" + id);
            };
        }
    }

    // @DynamicPropertySource
    // static void properties(final DynamicPropertyRegistry registry) {
    //     final String id = UUID.randomUUID().toString();
    //
    //     registry.add("spring.datasource.driver-class-name", DatabaseDriver.DERBY::getDriverClassName);
    //     registry.add("spring.datasource.url", () -> "jdbc:derby:memory:" + id + ";create=true");
    //     registry.add("spring.datasource.username", () -> "sa");
    //     registry.add("spring.datasource.password", () -> "");
    //
    //     registry.add("spring.datasource.hikari.pool-name", () -> "Hikari-Derby-" + id);
    // }
}
