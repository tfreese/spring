// Created: 29.11.2021
package de.freese.spring.hateoas;

import static org.junit.jupiter.api.Assertions.assertEquals;

import jakarta.annotation.Resource;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

/**
 * @author Thomas Freese
 */
@WebFluxTest(GreetingController.class) // braucht spring-boot-starter-webflux
@ActiveProfiles("test")
class GreetingWebFluxTests {
    @Resource
    private WebTestClient webTestClient;

    @Test
    @Disabled("No Encoder for org.springframework.hateoas.EntityModel -> muss konfiguriert werden")
    void testDefault() throws Exception {
        // @formatter:off
        this.webTestClient.get()
            .uri("/greeter")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("greeting").isEqualTo("Hello, World!")
            .jsonPath("_links.self.href").isEqualTo("http://localhost/greeter?name=World")
            .jsonPath("_links.forPath.href").isEqualTo("http://localhost/greeter/path/World")
            .jsonPath("_links.forPojo.href").isEqualTo("http://localhost/greeter/pojo?name=World")
            .jsonPath("_links.forSimple.href").isEqualTo("http://localhost/greeter/simple?name=World")
            ;
        // @formatter:on
    }

    @Test
    void testFail() throws Exception {
        // @formatter:off
        this.webTestClient.get()
            .uri("/greeter/fail")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isBadRequest()
            ;
        // @formatter:on
    }

    @Test
    @Disabled("No Encoder for org.springframework.hateoas.EntityModel -> muss konfiguriert werden")
    void testPath() throws Exception {
        // @formatter:off
        this.webTestClient.get()
            .uri("/greeter/path/Test")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("greeting").isEqualTo("Hello, Test!")
            .jsonPath("_links.self.href").isEqualTo("http://localhost/greeter/path/Test")
            .jsonPath("_links.forPojo.href").isEqualTo("http://localhost/greeter/pojo?name=Test")
            .jsonPath("_links.forSimple.href").isEqualTo("http://localhost/greeter/simple?name=Test")
            ;
        // @formatter:on
    }

    @Test
    @Disabled("No Encoder for org.springframework.hateoas.EntityModel -> muss konfiguriert werden")
    void testPojo() throws Exception {
        // @formatter:off
        this.webTestClient.get()
            .uri(uriBuilder -> uriBuilder
                    .path("/greeter/pojo")
                    .queryParam("name", "Test")
                    .build()
            )
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("greeting").isEqualTo("Hello, Test!")
            .jsonPath("_links.self.href").isEqualTo("http://localhost/greeter/pojo?name=Test")
            .jsonPath("_links.forPath.href").isEqualTo("http://localhost/greeter/path/Test")
            .jsonPath("_links.forSimple.href").isEqualTo("http://localhost/greeter/simple?name=Test")
            ;
        // @formatter:on
    }

    @Test
    void testSimple() throws Exception {
        // @formatter:off
        final String response = this.webTestClient.get()
            .uri("/greeter/simple")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isOk()
            .returnResult(String.class)
            .getResponseBody()
            .blockFirst()
            ;
        // @formatter:on

        final DocumentContext documentContext = JsonPath.parse(response);
        assertEquals("Hello, World!", documentContext.read("greeting", String.class));

        // @formatter:off
        this.webTestClient.get()
            .uri(uriBuilder -> uriBuilder
                    .path("/greeter/simple")
                    .queryParam("name", "Test")
                    .build()
            )
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("greeting").isEqualTo("Hello, Test!")
            ;
        // @formatter:on
    }
}
