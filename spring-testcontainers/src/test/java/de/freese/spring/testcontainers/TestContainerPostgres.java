package de.freese.spring.testcontainers;

import org.junit.jupiter.api.Disabled;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * @author Thomas Freese
 */
@Testcontainers
@Disabled("Postgres löscht die Sequence nicht")
class TestContainerPostgres extends AbstractTest
{
    @Container
    private static final PostgreSQLContainer DB_CONTAINER = new PostgreSQLContainer("postgres:latest");

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry)
    {
        registry.add("spring.datasource.driver-class-name", DB_CONTAINER::getDriverClassName);
        registry.add("spring.datasource.url", DB_CONTAINER::getJdbcUrl);
        registry.add("spring.datasource.username", DB_CONTAINER::getUsername);
        registry.add("spring.datasource.password", DB_CONTAINER::getPassword);

        registry.add("spring.datasource.hikari.pool-name", () -> "Hikari-" + DB_CONTAINER.getDockerImageName());
    }
}
