// Created: 09.02.2019
package de.freese.spring.cloud.gateway;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

import javax.annotation.Resource;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

/***
 * @author Thomas Freese
 */
// @RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Disabled("Funktioniert nur zusammen mit spring-cloud-eureka und spring-cloud-microservice")
class GatewayTest
{
    /**
     *
     */
    @Resource
    private WebTestClient webClient;

    /**
     *
     */
    @Test
    void testCircuitbreaker()
    {
        // @formatter:off
        this.webClient
            .get().uri("/delay/1")
            .header("Host", "www.circuitbreaker.com")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .consumeWith(response -> assertThat(response.getResponseBody()).isEqualTo("fallback\n".getBytes()));
        // @formatter:on
    }

    /**
     * @throws Exception Falls was schiefgeht.
     */
    @Test
    void testContextLoads() throws Exception
    {
        assertTrue(true);
    }

    /**
     *
     */
    @Test
    void testGet()
    {
        // @formatter:off
        this.webClient
            .get().uri("/get")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.headers.Hello").isEqualTo("World");
        // @formatter:on
    }

    /**
     * Funktioniert nur zusammen mit spring-eureka und spring-microservice.
     */
    @Test
    void testHello()
    {
        // @formatter:off
       this.webClient
           .get().uri("/hello")
           .exchange()
           .expectStatus().isOk()
           .expectBody()
           .consumeWith(response -> assertThat(response.getResponseBody()).isNotEmpty());
       // @formatter:on
    }

    /**
     * Funktioniert nur zusammen mit spring-eureka und spring-microservice.
     */
    @Test
    void testHelloLb()
    {
        // @formatter:off
       this.webClient
           .get().uri("/lb")
           .exchange()
           .expectStatus().isOk()
           .expectBody()
           .consumeWith(response -> assertThat(response.getResponseBody()).isNotEmpty());
       // @formatter:on
    }

    /**
     * Funktioniert nur zusammen mit spring-eureka und spring-microservice.
     */
    @Test
    @Disabled
    void testHelloLbManuell()
    {
        // @formatter:off
       this.webClient
           .get().uri("/lbman")
           .exchange()
           .expectStatus().isOk()
           .expectBody()
           .consumeWith(response -> assertThat(response.getResponseBody()).isNotEmpty());
       // @formatter:on
    }
}
