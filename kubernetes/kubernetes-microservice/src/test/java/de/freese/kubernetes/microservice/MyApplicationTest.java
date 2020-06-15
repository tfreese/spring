package de.freese.kubernetes.microservice;

import static org.junit.Assert.assertTrue;
import org.hamcrest.core.StringStartsWith;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import de.freese.kubernetes.microservice.MyApplication.MyRestController;

/**
 * @author Thomas Freese
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.Alphanumeric.class)
class MyApplicationTest
{
    /**
     *
     */
    @Test
    void contextLoads()
    {
        assertTrue(true);
    }

    /**
    *
    */
    @Test
    void request()
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
