package de.freese.spring.testcontainers;

import org.junit.jupiter.api.Disabled;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;

/**
 * @author Thomas Freese
 */
@Disabled("Postgres löscht die Sequence nicht")
class TestContainerPostgres extends AbstractTest {
    @Container
    @ServiceConnection // Ersetzt @DynamicPropertySource
    private static final PostgreSQLContainer<?> DB_CONTAINER = new PostgreSQLContainer<>("postgres:latest");

    //    @DynamicPropertySource
    //    static void properties(final DynamicPropertyRegistry registry) {
    //        registry.add("spring.datasource.driver-class-name", DB_CONTAINER::getDriverClassName);
    //        registry.add("spring.datasource.url", DB_CONTAINER::getJdbcUrl);
    //        registry.add("spring.datasource.username", DB_CONTAINER::getUsername);
    //        registry.add("spring.datasource.password", DB_CONTAINER::getPassword);
    //
    //        registry.add("spring.datasource.hikari.pool-name", () -> "Hikari-" + DB_CONTAINER.getDockerImageName());
    //    }
}
