// Created: 21.06.2019
package de.freese.spring.reactive.web;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.test.StepVerifier;

import de.freese.spring.reactive.model.Department;
import de.freese.spring.reactive.model.Employee;

/**
 * @author Thomas Freese
 */
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public interface TestWeb {
    @AfterEach
    default void afterEach() {
        doAfterEach();
    }

    @BeforeEach
    default void beforeEach() {
        doBeforeEach();
    }

    void doAfterEach();

    void doBeforeEach();

    WebClient getWebClient();

    WebTestClient getWebTestClient();

    @Test
    default void testCreateNewEmployee() {
        getWebTestClient()
                .put()
                .uri("employee")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON).acceptCharset(StandardCharsets.UTF_8)
                //.body(BodyInserters.fromObject(newEmployee))
                .bodyValue(new Employee("Foo", "Bar", "Dep3")) // ist das gleiche wie '.body(BodyInserters.fromObject(newEmployee))'
                .exchange() // Liefert auch Header und Status.
                .expectStatus().isOk()
                .expectHeader().valueEquals("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .expectBody(Employee.class)
                .isEqualTo(new Employee("Foo", "Bar", "Dep3", 4))
        ;

        getWebTestClient()
                .get()
                .uri("employees")
                .accept(MediaType.APPLICATION_JSON).acceptCharset(StandardCharsets.UTF_8)
                .exchange() // Liefert auch Header und Status.
                .expectStatus().isOk()
                .expectHeader().valueEquals("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .expectBodyList(Employee.class)
                .hasSize(4)
        ;

        getWebClient()
                .put()
                .uri("employee")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON).acceptCharset(StandardCharsets.UTF_8)
                .bodyValue(new Employee("Fooo", "Barr", "Dep3"))
                .retrieve()
                .bodyToMono(Employee.class)
                .as(StepVerifier::create)
                //            .expectNext(List.of(...)).as("number of departments")
                .expectNextMatches(emp -> emp.equals(new Employee("Fooo", "Barr", "Dep3", 5)))
                .verifyComplete()
        ;

        getWebClient()
                .get()
                .uri("employees")
                .accept(MediaType.APPLICATION_JSON).acceptCharset(StandardCharsets.UTF_8)
                .retrieve()
                .bodyToFlux(Employee.class)
                .as(StepVerifier::create)
                .expectNextCount(5)
                .verifyComplete()
        ;
    }

    @Test
    default void testDeleteEmployee() {
        getWebTestClient()
                .delete()
                .uri("employee/id/{id}", 1)
                .accept(MediaType.APPLICATION_JSON).acceptCharset(StandardCharsets.UTF_8)
                //.syncBody(newEmployee) // ist das gleiche wie '.body(BodyInserters.fromObject(newEmployee))'
                .exchange() // Liefert auch Header und Status.
                .expectStatus().isOk()
                .expectHeader().valueEquals("Content-Type", MediaType.APPLICATION_JSON_VALUE)
        ;

        getWebTestClient()
                .get()
                .uri("employees")
                .accept(MediaType.APPLICATION_JSON).acceptCharset(StandardCharsets.UTF_8)
                .exchange() // Liefert auch Header und Status.
                .expectStatus().isOk()
                .expectHeader().valueEquals("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .expectBodyList(Employee.class)
                .hasSize(2)
        ;

        getWebClient()
                .delete()
                .uri("employee/id/{id}", 2)
                .accept(MediaType.APPLICATION_JSON).acceptCharset(StandardCharsets.UTF_8)
                .retrieve()
                .bodyToMono(Void.class)
                .as(StepVerifier::create)
                .verifyComplete()
        ;

        getWebClient()
                .get()
                .uri("employees")
                .accept(MediaType.APPLICATION_JSON).acceptCharset(StandardCharsets.UTF_8)
                .retrieve()
                .bodyToFlux(Employee.class)
                .as(StepVerifier::create)
                .expectNextCount(1)
                .verifyComplete()
        ;
    }

    @Test
    default void testGetAllDepartments() {
        getWebTestClient()
                .get()
                .uri("departments")
                .accept(MediaType.APPLICATION_JSON).acceptCharset(StandardCharsets.UTF_8)
                .exchange() // Liefert auch Header und Status.
                .expectStatus().isOk()
                .expectHeader().valueEquals("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .expectBodyList(Employee.class).hasSize(3)
        ;

        getWebClient()
                .get()
                .uri("departments")
                .accept(MediaType.APPLICATION_JSON).acceptCharset(StandardCharsets.UTF_8)
                .retrieve()
                .bodyToFlux(Department.class)
                .as(StepVerifier::create)
                // .expectNext(List.of(...)).as("number of departments")
                .expectNextCount(3)
                .verifyComplete()
        ;
    }

    @Test
    default void testGetAllEmployees() {
        getWebTestClient()
                .get()
                .uri("employees")
                .accept(MediaType.APPLICATION_JSON).acceptCharset(StandardCharsets.UTF_8)
                .exchange() // Liefert auch Header und Status.
                .expectStatus().isOk()
                .expectHeader().valueEquals("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .expectBodyList(Employee.class).hasSize(3)
        ;

        getWebClient()
                .get()
                .uri("employees")
                .accept(MediaType.APPLICATION_JSON).acceptCharset(StandardCharsets.UTF_8)
                .retrieve()
                .bodyToFlux(Employee.class)
                .as(StepVerifier::create)
                .expectNextCount(3)
                .verifyComplete()
        ;
    }

    @Test
    default void testGetEmployee() {
        getWebTestClient()
                .get()
                .uri("employee/ln/{lastName}/fn/{firstName}", "LastName1", "FirstName1")
                .accept(MediaType.APPLICATION_JSON).acceptCharset(StandardCharsets.UTF_8)
                .exchange() // Liefert auch Header und Status.
                .expectStatus().isOk()
                .expectHeader().valueEquals("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .expectBody(Employee.class).isEqualTo(new Employee("LastName1", "FirstName1", "Dep1", 1))
        ;

        getWebClient()
                .get()
                .uri("employee/ln/{lastName}/fn/{firstName}", "LastName1", "FirstName1")
                .accept(MediaType.APPLICATION_JSON).acceptCharset(StandardCharsets.UTF_8)
                .retrieve()
                .bodyToMono(Employee.class)
                .as(StepVerifier::create)
                .expectNextMatches(emp -> emp.equals(new Employee("LastName1", "FirstName1", "Dep1", 1)))
                .verifyComplete()
        ;
    }
}
