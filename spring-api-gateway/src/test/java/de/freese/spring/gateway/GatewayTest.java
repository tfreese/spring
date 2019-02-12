/*** Created:09.02.2019 */

package de.freese.spring.gateway;

import static org.assertj.core.api.Assertions.assertThat;
import javax.annotation.Resource;
import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;

/***
 * @author Thomas Freese
 */
// @Ignore
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
// , properties =
// {
// "httpbin=http://localhost:${wiremock.server.port}"
// }
)
// @AutoConfigureWireMock(port = 0)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class GatewayTest
{
    /**
    *
    */
    @Resource
    private WebTestClient webClient = null;

    /**
     * Erstellt ein neues {@link GatewayTest} Object.
     */
    public GatewayTest()
    {
        super();
    }

    /**
     * @throws Exception Falls was schief geht.
     */
    @Test
    public void test010ContextLoads() throws Exception
    {
    }

    /**
     *
     */
    @Test
    public void test020Get()
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
    @Ignore
    public void test030Sysdate()
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
    @Ignore
    public void test040SysdateLB()
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
    public void test050Hystrix()
    {
        // @formatter:off
        this.webClient
            .get().uri("/delay/1")
            .header("Host", "www.hystrix.com")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .consumeWith(response -> assertThat(response.getResponseBody()).isEqualTo("fallback".getBytes()));
        // @formatter:on
    }
}
