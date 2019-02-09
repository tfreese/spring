/**
 * Created: 09.02.2019
 */

package de.freese.spring.gateway;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import javax.annotation.Resource;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;

/**
 * @author Thomas Freese
 */
@Ignore
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties =
{
        "httpbin=http://localhost:${wiremock.server.port}"
})
@AutoConfigureWireMock(port = 0)
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
    public void contextLoads() throws Exception
    {
        // @formatter:off
        stubFor(get(urlEqualTo("/get"))
                .willReturn(aResponse()
                    .withBody("{\"headers\":{\"Hello\":\"World\"}}")
                    .withHeader("Content-Type", "application/json")));

        stubFor(get(urlEqualTo("/delay/3"))
            .willReturn(aResponse()
                .withBody("no fallback")
                .withFixedDelay(3000)));

        this.webClient
            .get().uri("/get")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.headers.Hello").isEqualTo("World");

        this.webClient
            .get().uri("/delay/3")
            .header("Host", "www.hystrix.com")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .consumeWith(
                response -> assertThat(response.getResponseBody()).isEqualTo("fallback".getBytes()));
        // @formatter:on
    }
}
