/**
 * Created: 21.06.2019
 */

package de.freese.spring.reactive;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.client.WebClient;
import de.freese.spring.reactive.model.Department;
import de.freese.spring.reactive.model.Employee;
import reactor.test.StepVerifier;

/**
 * @author Thomas Freese
 */
public interface TestInterface
{
    /**
     *
     */
    @Test
    // @DirtiesContext(classMode = ClassMode.BEFORE_EACH_TEST_METHOD)
    // @Sql(executionPhase=ExecutionPhase.BEFORE_TEST_METHOD,scripts="classpath:/test-sql/group2.sql")
    default void createNewEmployee()
    {

    }

    /**
      *
      */
    @Test
    default void deleteEmployee()
    {

    }

    /**
    *
    */
    @Test
    default void getAllDepartments()
    {
        // @formatter:off
        getWebTestClient()
            .get()
            .uri("departments")
            .accept(MediaType.APPLICATION_JSON)
            .exchange() // // Liefert auch Header und Status.
            .expectStatus().isOk()
            .expectHeader().valueEquals("Content-Type", MediaType.APPLICATION_JSON_UTF8_VALUE)
            ;
        // @formatter:on

        // @formatter:off
        getWebClient()
            .get()
            .uri("departments")
            .accept(MediaType.APPLICATION_JSON)
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
    default void getAllEmployees()
    {
        // @formatter:off
        getWebTestClient()
            .get()
            .uri("employees")
            .accept(MediaType.APPLICATION_JSON)
            .exchange() // // Liefert auch Header und Status.
            .expectStatus().isOk()
            .expectHeader().valueEquals("Content-Type", MediaType.APPLICATION_JSON_UTF8_VALUE)
            ;
        // @formatter:on

        // @formatter:off
        getWebClient()
          .get()
          .uri("employees")
          .accept(MediaType.APPLICATION_JSON)
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
    default void getEmployee()
    {

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
