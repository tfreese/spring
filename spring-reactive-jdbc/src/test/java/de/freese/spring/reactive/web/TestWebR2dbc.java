// Created: 21.06.2019
package de.freese.spring.reactive.web;

import jakarta.annotation.Resource;

import io.r2dbc.spi.ConnectionFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.r2dbc.connection.init.ResourceDatabasePopulator;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * @author Thomas Freese
 */
@ActiveProfiles({"test", "r2dbc"})
@EnableAutoConfiguration(exclude = DataSourceAutoConfiguration.class)
class TestWebR2dbc implements TestWeb {
    @Resource
    private ConnectionFactory connectionFactory;

    @Resource
    private DatabaseClient databaseClient;

    @Value("${local.server.port}")
    private int port;

    private WebClient webClient;

    @Resource
    private WebTestClient webTestClient;

    @Override
    public void doAfterEach() {
        this.databaseClient.sql("DROP TABLE IF EXISTS employee").fetch().rowsUpdated().block();
        this.databaseClient.sql("DROP TABLE IF EXISTS department").fetch().rowsUpdated().block();
    }

    @Override
    public void doBeforeEach() {
        this.webClient = WebClient.create("http://localhost:" + this.port);

        final ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
        populator.addScript(new ClassPathResource("sql/schema.sql"));
        populator.addScript(new ClassPathResource("sql/data.sql"));
        populator.populate(this.connectionFactory).block();
    }

    @Override
    public WebClient getWebClient() {
        return this.webClient;
    }

    @Override
    public WebTestClient getWebTestClient() {
        return this.webTestClient;
    }

    @Override
    @Test
    public void testDeleteEmployee() {
        // nur zum Debuggen
        TestWeb.super.testDeleteEmployee();
    }
}
