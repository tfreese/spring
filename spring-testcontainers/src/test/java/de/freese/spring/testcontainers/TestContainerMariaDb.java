package de.freese.spring.testcontainers;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MariaDBContainer;
import org.testcontainers.junit.jupiter.Container;

/**
 * @author Thomas Freese
 */
class TestContainerMariaDb extends AbstractTest {
    /**
     * will be shared between test methods<br />
     * DockerImageName.parse(MariaDBContainer.NAME)
     * mariadb:latest<br />
     */
    @Container
    private static final MariaDBContainer<?> DB_CONTAINER = new MariaDBContainer<>("mariadb:latest");

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.driver-class-name", DB_CONTAINER::getDriverClassName);
        registry.add("spring.datasource.url", DB_CONTAINER::getJdbcUrl);
        registry.add("spring.datasource.username", DB_CONTAINER::getUsername);
        registry.add("spring.datasource.password", DB_CONTAINER::getPassword);

        registry.add("spring.datasource.hikari.pool-name", () -> "Hikari-" + DB_CONTAINER.getDockerImageName());
    }

    //    /**
    //     * will be started before and stopped after each test method<br />
    //     * DockerImageName.parse(MariaDBContainer.NAME)
    //     * mariadb:latest
    //     */
    //    @Container
    //    private MariaDBContainer mariaDBContainer = new MariaDBContainer(DockerImageName.parse(MariaDBContainer.NAME));
    //    //            .withDatabaseName("foo")
    //    //            .withUsername("foo")
    //    //            .withPassword("secret");

    //    @Bean
    //    DataSource dataSource()
    //    {
    //        HikariConfig config = new HikariConfig();
    //        config.setDriverClassName(mariaDBContainer.getDriverClassName());
    //        config.setJdbcUrl(mariaDBContainer.getJdbcUrl());
    //        config.setUsername(mariaDBContainer.getUsername());
    //        config.setPassword(mariaDBContainer.getPassword());
    //
    //        return new HikariDataSource(config);
    //    }
}
