// Created: 21.06.2019
package de.freese.spring.reactive.web;

import javax.annotation.Resource;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.core.io.ClassPathResource;
import org.springframework.r2dbc.connection.init.ResourceDatabasePopulator;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.client.WebClient;

import io.r2dbc.spi.ConnectionFactory;

/**
 * @author Thomas Freese
 */
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles(
{
        "test", "r2dbc"
})
@EnableAutoConfiguration(exclude = DataSourceAutoConfiguration.class)
class TestWebR2dbc implements TestWeb
{
    /**
    *
    */
    @Resource
    private ConnectionFactory connectionFactory;
    /**
    *
    */
    @Resource
    private DatabaseClient databaseClient;
    /**
    *
    */
    @LocalServerPort
    private int port = -1;
    /**
    *
    */
    private WebClient webClient;
    /**
    *
    *
    */
    @Resource
    private WebTestClient webTestClient;

    /**
     * @see de.freese.spring.reactive.web.TestWeb#doAfterEach()
     */
    @Override
    public void doAfterEach()
    {
        this.databaseClient.sql("DROP TABLE IF EXISTS employee").fetch().rowsUpdated().block();
        this.databaseClient.sql("DROP TABLE IF EXISTS department").fetch().rowsUpdated().block();
    }

    /**
     * @see de.freese.spring.reactive.web.TestWeb#doBeforeEach()
     */
    @Override
    public void doBeforeEach()
    {
        this.webClient = WebClient.create("http://localhost:" + this.port);

        ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
        populator.addScript(new ClassPathResource("sql/schema-h2.sql"));
        populator.addScript(new ClassPathResource("sql/data.sql"));
        populator.populate(this.connectionFactory).block();
    }

    /**
     * @see de.freese.spring.reactive.web.TestWeb#getWebClient()
     */
    @Override
    public WebClient getWebClient()
    {
        return this.webClient;
    }

    /**
     * @see de.freese.spring.reactive.web.TestWeb#getWebTestClient()
     */
    @Override
    public WebTestClient getWebTestClient()
    {
        return this.webTestClient;
    }

    /**
     * @see de.freese.spring.reactive.web.TestWeb#testDeleteEmployee()
     */
    @Override
    @Test
    public void testDeleteEmployee()
    {
        // nur zum Debuggen
        TestWeb.super.testDeleteEmployee();
    }

}