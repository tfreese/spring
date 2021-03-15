// Created: 09.02.2019
package de.freese.spring.gateway;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import javax.annotation.Resource;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

/***
 * @author Thomas Freese
 */
// @RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT
// , properties =
// {
// "httpbin=http://localhost:${wiremock.server.port}"
// }
)
// @AutoConfigureWireMock(port = 0)
@TestMethodOrder(MethodOrderer.MethodName.class)
@ActiveProfiles("test")
class GatewayTest
{
    /**
    *
    */
    @Resource
    private WebTestClient webClient;

    /**
     * @throws Exception Falls was schief geht.
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
    @Disabled("Funktioniert nur zusammen mit spring-microservice")
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
     *
     */
    @Test
    @Disabled("Funktioniert nur zusammen mit spring-microservice")
    void testHystrix()
    {
        // @formatter:off
        this.webClient
            .get().uri("/delay/1")
            .header("Host", "www.hystrix.com")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .consumeWith(response -> assertThat(response.getResponseBody()).isEqualTo("fallback\n".getBytes()));
        // @formatter:on
    }

    /**
     * Separater Server wird benötigt -> spring-microservice
     */
    @Test
    @Disabled("Funktioniert nur zusammen mit spring-microservice")
    void testSysdate()
    {
       // @formatter:off
       this.webClient
           .get().uri("/sysdate")
           .exchange()
           .expectStatus().isOk()
           .expectBody()
           .consumeWith(response -> assertThat(response.getResponseBody()).isNotEmpty());
           ;
       // @formatter:on
    }

    /**
     * Separater Server wird benötigt -> spring-microservice
     */
    @Test
    @Disabled("Funktioniert nur zusammen mit spring-microservice")
    void testSysdateLB()
    {
       // @formatter:off
       this.webClient
           .get().uri("/sysdatelb")
           .exchange()
           .expectStatus().isOk()
           .expectBody()
           .consumeWith(response -> assertThat(response.getResponseBody()).isNotEmpty());
           ;
       // @formatter:on
    }
}
