package de.freese.kubernetes.microservice;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.hamcrest.core.StringStartsWith;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

/**
 * @author Thomas Freese
 */
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class MyApplicationTest {
    @Test
    void testContextLoads() {
        assertTrue(true);
    }

    @Test
    void testRequest() {
        final WebTestClient webClient = WebTestClient.bindToController(new MyRestController()).build();

        //@formatter:off
        webClient
            .get()
            // .uri("/greet")
            .uri(uriBuilder -> uriBuilder
                    .path("/")
                    //.queryParam("name", "Tommy")
                    .build())
            .accept(MediaType.TEXT_PLAIN)
            .exchange()
            .expectStatus().isOk()
            //.expectBody(String.class).isEqualTo("Hello, Spring!")
            .expectBody(String.class).value(StringStartsWith.startsWith("Hello"))
            ;
        //@formatter:on
    }
}
