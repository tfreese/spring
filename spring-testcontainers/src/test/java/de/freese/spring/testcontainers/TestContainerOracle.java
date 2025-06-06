package de.freese.spring.testcontainers;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.testcontainers.containers.OracleContainer;
import org.testcontainers.junit.jupiter.Container;

/**
 * @author Thomas Freese
 */
class TestContainerOracle extends AbstractTest {
    @Container
    @ServiceConnection // Ersetzt @DynamicPropertySource
    private static final OracleContainer DB_CONTAINER = new OracleContainer("gvenzl/oracle-xe:latest");

    //    @DynamicPropertySource
    //    static void properties(final DynamicPropertyRegistry registry) {
    //        registry.add("spring.datasource.driver-class-name", DB_CONTAINER::getDriverClassName);
    //        registry.add("spring.datasource.url", DB_CONTAINER::getJdbcUrl);
    //        registry.add("spring.datasource.username", DB_CONTAINER::getUsername);
    //        registry.add("spring.datasource.password", DB_CONTAINER::getPassword);
    //
    //        registry.add("spring.datasource.hikari.pool-name", () -> "Hikari-" + DB_CONTAINER.getDockerImageName());
    //    }

    @Override
    @BeforeEach
    void beforeEach() {
        final ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
        populator.addScript(new ClassPathResource("db-schema-oracle.sql"));
        populator.execute(getDataSource());
    }
}
