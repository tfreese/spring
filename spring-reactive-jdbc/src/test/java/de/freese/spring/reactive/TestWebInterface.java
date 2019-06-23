/**
 * Created: 21.06.2019
 */

package de.freese.spring.reactive;

import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.client.WebClient;
import de.freese.spring.reactive.model.Department;
import de.freese.spring.reactive.model.Employee;
import reactor.test.StepVerifier;

/**
 * @author Thomas Freese
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public interface TestWebInterface
{
    /**
     *
     */
    @Test
    @Order(20)
    // @Disabled
    // @DirtiesContext(classMode = ClassMode.BEFORE_EACH_TEST_METHOD)
    // @Sql(executionPhase=ExecutionPhase.BEFORE_TEST_METHOD,scripts="classpath:/test-sql/group2.sql")
    default void createNewEmployee()
    {
        Employee newEmployee = new Employee("Foo", "Bar", "Manufacturing");
        Employee expected = new Employee(7, "Foo", "Bar", "Manufacturing");

        // @formatter:off
        getWebTestClient()
            .put()
            .uri("employee")
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON).acceptCharset(StandardCharsets.UTF_8)
            //.body(BodyInserters.fromObject(newEmployee))
            .syncBody(newEmployee) // ist das gleiche wie '.body(BodyInserters.fromObject(newEmployee))'
            .exchange() // Liefert auch Header und Status.
            .expectStatus().isOk()
            .expectHeader().valueEquals("Content-Type", MediaType.APPLICATION_JSON_UTF8_VALUE)
            .expectBody(Employee.class).isEqualTo(expected)
            ;
        // @formatter:on

        // @formatter:off
        getWebTestClient()
            .get()
            .uri("employees")
            .accept(MediaType.APPLICATION_JSON).acceptCharset(StandardCharsets.UTF_8)
            .exchange() // Liefert auch Header und Status.
            .expectStatus().isOk()
            .expectHeader().valueEquals("Content-Type", MediaType.APPLICATION_JSON_UTF8_VALUE)
            .expectBodyList(Employee.class).hasSize(7)
            ;
        // @formatter:on

        newEmployee = new Employee("Fooo", "Barr", "Manufacturing");
        expected = new Employee(8, "Foor", "Barr", "Manufacturing");

//        // @formatter:off
//        getWebClient()
//            .put()
//            .uri("employee")
//            .contentType(MediaType.APPLICATION_JSON)
//            .accept(MediaType.APPLICATION_JSON).acceptCharset(StandardCharsets.UTF_8)
//            .syncBody(newEmployee)
//            .exchange() // Liefert auch Header und Status.
//            .single()&&bodyToFlux(Department.class)
//            .as(StepVerifier::create)
////            .expectNext(List.of(...)).as("number of departments")
//            .expectNextCount(5)
//            .verifyComplete()
//            ;
//        // @formatter:on
    }

    /**
      *
      */
    @Test
    @Order(10)
    default void deleteEmployee()
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
            .expectHeader().valueEquals("Content-Type", MediaType.APPLICATION_JSON_UTF8_VALUE)
            .expectBodyList(Employee.class).hasSize(5)
            ;
        // @formatter:on
    }

    /**
    *
    */
    @Test
    @Order(1)
    default void getAllDepartments()
    {
        // @formatter:off
        getWebTestClient()
            .get()
            .uri("departments")
            .accept(MediaType.APPLICATION_JSON).acceptCharset(StandardCharsets.UTF_8)
            .exchange() // Liefert auch Header und Status.
            .expectStatus().isOk()
            .expectHeader().valueEquals("Content-Type", MediaType.APPLICATION_JSON_UTF8_VALUE)
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
    @Order(1)
    default void getAllEmployees()
    {
        // @formatter:off
        getWebTestClient()
            .get()
            .uri("employees")
            .accept(MediaType.APPLICATION_JSON).acceptCharset(StandardCharsets.UTF_8)
            .exchange() // Liefert auch Header und Status.
            .expectStatus().isOk()
            .expectHeader().valueEquals("Content-Type", MediaType.APPLICATION_JSON_UTF8_VALUE)
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
    @Order(1)
    default void getEmployee()
    {
        // @formatter:off
        getWebTestClient()
            .get()
            .uri("employee/fn/{firstName}/ln/{lastName}", "Sally", "Wilson")
            .accept(MediaType.APPLICATION_JSON).acceptCharset(StandardCharsets.UTF_8)
            .exchange() // Liefert auch Header und Status.
            .expectStatus().isOk()
            .expectHeader().valueEquals("Content-Type", MediaType.APPLICATION_JSON_VALUE)
            .expectBody(Employee.class).isEqualTo(new Employee(3, "Sally","Wilson", "Human Resources"))
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
          .expectNextMatches(emp -> emp.equals(new Employee(3, "Sally", "Wilson", "Human Resources")))
          .verifyComplete()
          ;
        // @formatter:on
    }

    /**
     * @return {@link WebClient}
     */
    WebClient getWebClient();

    /**
     * @return {@link WebTestClient}
     */
    WebTestClient getWebTestClient();
}
