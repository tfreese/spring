// Created: 21.06.2019
package de.freese.spring.reactive.web;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.client.WebClient;

import de.freese.spring.reactive.model.Department;
import de.freese.spring.reactive.model.Employee;
import reactor.test.StepVerifier;

/**
 * @author Thomas Freese
 */
@SpringBootTest
@ActiveProfiles("test")
public interface TestWeb
{
    /**
    *
    */
    @AfterEach
    default void afterEachTables()
    {
        getJdbcTemplate().execute("DROP TABLE employee");
        getJdbcTemplate().execute("DROP TABLE department");
    }

    /**
    *
    */
    @BeforeEach
    default void beforeEachTables()
    {
        ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
        populator.addScript(new ClassPathResource("sql/schema-h2.sql"));
        populator.addScript(new ClassPathResource("sql/data.sql"));
        populator.execute(getJdbcTemplate().getDataSource());
    }

    /**
     * @return {@link JdbcTemplate}
     */
    JdbcTemplate getJdbcTemplate();

    /**
     * @return {@link WebClient}
     */
    WebClient getWebClient();

    /**
     * @return {@link WebTestClient}
     */
    WebTestClient getWebTestClient();

    /**
     *
     */
    @Test
    default void testCreateNewEmployee()
    {
        // @formatter:off
        getWebTestClient()
            .put()
            .uri("employee")
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON).acceptCharset(StandardCharsets.UTF_8)
            //.body(BodyInserters.fromObject(newEmployee))
            .bodyValue(new Employee("Foo", "Bar", "Manufacturing")) // ist das gleiche wie '.body(BodyInserters.fromObject(newEmployee))'
            .exchange() // Liefert auch Header und Status.
            .expectStatus().isOk()
            .expectHeader().valueEquals("Content-Type", MediaType.APPLICATION_JSON_VALUE)
            .expectBody(Employee.class).isEqualTo(new Employee("Foo", "Bar", "Manufacturing", 7))
            ;
        // @formatter:on

        // @formatter:off
        getWebTestClient()
            .get()
            .uri("employees")
            .accept(MediaType.APPLICATION_JSON).acceptCharset(StandardCharsets.UTF_8)
            .exchange() // Liefert auch Header und Status.
            .expectStatus().isOk()
            .expectHeader().valueEquals("Content-Type", MediaType.APPLICATION_JSON_VALUE)
            .expectBodyList(Employee.class).hasSize(7)
            ;
        // @formatter:on

        // @formatter:off
        getWebClient()
            .put()
            .uri("employee")
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON).acceptCharset(StandardCharsets.UTF_8)
            .bodyValue(new Employee("Fooo", "Barr", "Manufacturing"))
            .retrieve()
            .bodyToMono(Employee.class)
            .as(StepVerifier::create)
//            .expectNext(List.of(...)).as("number of departments")
            .expectNextMatches(emp -> emp.equals(new Employee("Fooo", "Barr", "Manufacturing", 8)))
            .verifyComplete()
            ;
        // @formatter:on

        // @formatter:off
        getWebClient()
            .get()
            .uri("employees")
            .accept(MediaType.APPLICATION_JSON).acceptCharset(StandardCharsets.UTF_8)
            .retrieve()
            .bodyToFlux(Employee.class)
            .as(StepVerifier::create)
            .expectNextCount(8)
            .verifyComplete()
            ;
        // @formatter:on
    }

    /**
      *
      */
    @Test
    default void testDeleteEmployee()
    {
        // @formatter:off
        getWebTestClient()
            .delete()
            .uri("employee/id/{id}", 1)
            .accept(MediaType.APPLICATION_JSON).acceptCharset(StandardCharsets.UTF_8)
            //.syncBody(newEmployee) // ist das gleiche wie '.body(BodyInserters.fromObject(newEmployee))'
            .exchange() // Liefert auch Header und Status.
            .expectStatus().isOk()
            .expectHeader().valueEquals("Content-Type", MediaType.APPLICATION_JSON_VALUE)
            ;
        // @formatter:on

        // @formatter:off
        getWebTestClient()
            .get()
            .uri("employees")
            .accept(MediaType.APPLICATION_JSON).acceptCharset(StandardCharsets.UTF_8)
            .exchange() // Liefert auch Header und Status.
            .expectStatus().isOk()
            .expectHeader().valueEquals("Content-Type", MediaType.APPLICATION_JSON_VALUE)
            .expectBodyList(Employee.class).hasSize(5)
            ;
        // @formatter:on

        // @formatter:off
        getWebClient()
            .delete()
            .uri("employee/id/{id}", 2)
            .accept(MediaType.APPLICATION_JSON).acceptCharset(StandardCharsets.UTF_8)
            .retrieve()
            .bodyToMono(Void.class)
            .as(StepVerifier::create)
            .verifyComplete()
            ;
        // @formatter:on

        // @formatter:off
        getWebClient()
            .get()
            .uri("employees")
            .accept(MediaType.APPLICATION_JSON).acceptCharset(StandardCharsets.UTF_8)
            .retrieve()
            .bodyToFlux(Employee.class)
            .as(StepVerifier::create)
            .expectNextCount(4)
            .verifyComplete()
            ;
        // @formatter:on
    }

    /**
    *
    */
    @Test
    default void testGetAllDepartments()
    {
        // @formatter:off
        getWebTestClient()
            .get()
            .uri("departments")
            .accept(MediaType.APPLICATION_JSON).acceptCharset(StandardCharsets.UTF_8)
            .exchange() // Liefert auch Header und Status.
            .expectStatus().isOk()
            .expectHeader().valueEquals("Content-Type", MediaType.APPLICATION_JSON_VALUE)
            .expectBodyList(Employee.class).hasSize(5)
            ;
        // @formatter:on

        // @formatter:off
        getWebClient()
            .get()
            .uri("departments")
            .accept(MediaType.APPLICATION_JSON).acceptCharset(StandardCharsets.UTF_8)
            .retrieve()
            .bodyToFlux(Department.class)
            .as(StepVerifier::create)
//            .expectNext(List.of(...)).as("number of departments")
            .expectNextCount(5)
            .verifyComplete()
            ;
        // @formatter:on
    }

    /**
    *
    */
    @Test
    default void testGetAllEmployees()
    {
        // @formatter:off
        getWebTestClient()
            .get()
            .uri("employees")
            .accept(MediaType.APPLICATION_JSON).acceptCharset(StandardCharsets.UTF_8)
            .exchange() // Liefert auch Header und Status.
            .expectStatus().isOk()
            .expectHeader().valueEquals("Content-Type", MediaType.APPLICATION_JSON_VALUE)
            .expectBodyList(Employee.class).hasSize(6)
            ;
        // @formatter:on

        // @formatter:off
        getWebClient()
            .get()
            .uri("employees")
            .accept(MediaType.APPLICATION_JSON).acceptCharset(StandardCharsets.UTF_8)
            .retrieve()
            .bodyToFlux(Employee.class)
            .as(StepVerifier::create)
            .expectNextCount(6)
            .verifyComplete()
            ;
        // @formatter:on
    }

    /**
      *
      */
    @Test
    default void testGetEmployee()
    {
        // @formatter:off
        getWebTestClient()
            .get()
            .uri("employee/fn/{firstName}/ln/{lastName}", "Sally", "Wilson")
            .accept(MediaType.APPLICATION_JSON).acceptCharset(StandardCharsets.UTF_8)
            .exchange() // Liefert auch Header und Status.
            .expectStatus().isOk()
            .expectHeader().valueEquals("Content-Type", MediaType.APPLICATION_JSON_VALUE)
            .expectBody(Employee.class).isEqualTo(new Employee("Sally","Wilson", "Human Resources", 3))
            ;
        // @formatter:on

        // @formatter:off
        getWebClient()
            .get()
            .uri("employee/fn/{firstName}/ln/{lastName}", "Sally", "Wilson")
            .accept(MediaType.APPLICATION_JSON).acceptCharset(StandardCharsets.UTF_8)
            .retrieve()
            .bodyToMono(Employee.class)
            .as(StepVerifier::create)
            .expectNextMatches(emp -> emp.equals(new Employee("Sally", "Wilson", "Human Resources", 3)))
            .verifyComplete()
            ;
        // @formatter:on
    }
}
