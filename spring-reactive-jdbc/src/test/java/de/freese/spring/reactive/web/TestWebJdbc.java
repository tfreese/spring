// Created: 21.06.2019
package de.freese.spring.reactive.web;

import jakarta.annotation.Resource;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
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
@AutoConfigureWebTestClient
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
        jdbcTemplate.execute("DROP TABLE IF EXISTS employee");
        jdbcTemplate.execute("DROP TABLE IF EXISTS department");
    }

    @Override
    public void doBeforeEach() {
        webClient = WebClient.create("http://localhost:" + port);

        final ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
        populator.addScript(new ClassPathResource("sql/schema.sql"));
        populator.addScript(new ClassPathResource("sql/data.sql"));
        populator.execute(jdbcTemplate.getDataSource());
    }

    @Override
    public WebClient getWebClient() {
        return webClient;
    }

    @Override
    public WebTestClient getWebTestClient() {
        return webTestClient;
    }

    @Override
    @Test
    public void testGetEmployee() {
        // nur zum Debuggen
        TestWeb.super.testGetEmployee();
    }
}
