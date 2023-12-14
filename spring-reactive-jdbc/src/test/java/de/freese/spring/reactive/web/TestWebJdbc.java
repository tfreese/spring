// Created: 21.06.2019
package de.freese.spring.reactive.web;

import jakarta.annotation.Resource;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * @author Thomas Freese
 */
@ActiveProfiles({"test", "jdbc"})
class TestWebJdbc implements TestWeb {
    @Resource
    private JdbcTemplate jdbcTemplate;

    @Value("${local.server.port}")
    private int port;

    private WebClient webClient;

    @Resource
    private WebTestClient webTestClient;

    @Override
    public void doAfterEach() {
        this.jdbcTemplate.execute("DROP TABLE IF EXISTS employee");
        this.jdbcTemplate.execute("DROP TABLE IF EXISTS department");
    }

    @Override
    public void doBeforeEach() {
        this.webClient = WebClient.create("http://localhost:" + this.port);

        final ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
        populator.addScript(new ClassPathResource("sql/schema.sql"));
        populator.addScript(new ClassPathResource("sql/data.sql"));
        populator.execute(this.jdbcTemplate.getDataSource());
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
    public void testGetEmployee() {
        // nur zum Debuggen
        TestWeb.super.testGetEmployee();
    }
}
