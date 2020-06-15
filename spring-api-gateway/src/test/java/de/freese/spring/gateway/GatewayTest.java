/*** Created:09.02.2019 */

package de.freese.spring.gateway;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;
import javax.annotation.Resource;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

/***
 * @author Thomas Freese
 */
// @RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
// , properties =
// {
// "httpbin=http://localhost:${wiremock.server.port}"
// }
)
// @AutoConfigureWireMock(port = 0)
@TestMethodOrder(MethodOrderer.Alphanumeric.class)
@ActiveProfiles("test")
class GatewayTest
{
    /**
    *
    */
    @Resource
    private WebTestClient webClient = null;

    /**
     * @throws Exception Falls was schief geht.
     */
    @Test
    void test010ContextLoads() throws Exception
    {
        assertTrue(true);
    }

    /**
     *
     */
    @Test
    @Disabled("Funktioniert nur zusammen mit spring-microservice")
    void test020Get()
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
     * Separater Server wird benötigt -> spring-microservice
     */
    @Test
    @Disabled("Funktioniert nur zusammen mit spring-microservice")
    void test030Sysdate()
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
    void test040SysdateLB()
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

    /**
     *
     */
    @Test
    @Disabled("Funktioniert nur zusammen mit spring-microservice")
    void test050Hystrix()
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
}
