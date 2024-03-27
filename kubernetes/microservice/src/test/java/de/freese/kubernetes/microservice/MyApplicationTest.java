package de.freese.kubernetes.microservice;

import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.annotation.Resource;

import org.hamcrest.core.StringStartsWith;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.test.web.reactive.server.WebTestClient;

/**
 * for i in {1..20}; do curl localhost:8090; echo ""; sleep 0.1; done;
 *
 * @author Thomas Freese
 */
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT, properties = {"DB_URL=jdbc:h2:mem:memDb", "DB_USER=sa", "DB_PSW="})
        // properties = {"DB_URL=jdbc:h2:mem:memDb", "DB_USER=sa", "DB_PSW="}
        // properties = {"DB_URL=jdbc:h2:tcp://localhost:9082/file:fileDb", "DB_USER=sa", "DB_PSW="}
        // properties = {"DB_URL=jdbc:hsqldb:mem:memDb"}
        // properties = {"R2DBC_URL=r2dbc:h2:mem:///memDb?DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=true"}
class MyApplicationTest {

    // @Resource
    // private DatabaseClient databaseClient;

    @Resource
    private JdbcClient jdbcClient;

    @Test
    void testContextLoads() {
        assertTrue(true);
    }

    @Test
    void testRequest() {
        final WebTestClient webClient = WebTestClient.bindToController(new MyRestController(jdbcClient)).build();

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
