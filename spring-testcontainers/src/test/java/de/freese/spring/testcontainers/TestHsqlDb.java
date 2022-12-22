package de.freese.spring.testcontainers;

import java.util.UUID;

import org.springframework.boot.jdbc.DatabaseDriver;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

/**
 * @author Thomas Freese
 */
//@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD) // Jede Methode mit eigenem Context.
class TestHsqlDb extends AbstractTest
{
    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry)
    {
        String id = UUID.randomUUID().toString();

        registry.add("spring.datasource.driver-class-name", DatabaseDriver.HSQLDB::getDriverClassName);
        registry.add("spring.datasource.url", () -> "jdbc:hsqldb:mem:" + id + ";shutdown=true");
        registry.add("spring.datasource.username", () -> "sa");
        registry.add("spring.datasource.password", () -> "");

        registry.add("spring.datasource.hikari.pool-name", () -> "Hikari-HsqlDb-" + id);
    }
}
