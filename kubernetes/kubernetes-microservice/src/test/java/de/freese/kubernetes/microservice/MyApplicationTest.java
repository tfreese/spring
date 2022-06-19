package de.freese.kubernetes.microservice;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.hamcrest.core.StringStartsWith;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

/**
 * @author Thomas Freese
 */
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.MethodName.class)
class MyApplicationTest
{
    /**
     *
     */
    @Test
    void testContextLoads()
    {
        assertTrue(true);
    }

    /**
     *
     */
    @Test
    void testRequest()
    {
        WebTestClient webClient = WebTestClient.bindToController(new MyRestController()).build();

        //@formatter:off
        webClient
            .get()
            .uri("/greet")
            .accept(MediaType.TEXT_PLAIN)
            .exchange()
            .expectStatus().isOk()
            //.expectBody(String.class).isEqualTo("Hello, Spring!")
            .expectBody(String.class).value(StringStartsWith.startsWith("Hello World"))
            ;
        //@formatter:on
    }
}
