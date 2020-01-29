/**
 * Created: 07.09.2018
 */

package org.spring.jwt;

import java.io.IOException;
import java.util.Objects;
import javax.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.spring.jwt.token.JwtTokenProvider;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;
import com.jayway.jsonpath.internal.JsonFormatter;

/**
 * @author Thomas Freese
 */
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT, classes =
{
        JwtAuthorisationApplication.class
})
@TestMethodOrder(MethodOrderer.Alphanumeric.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
// @Disabled
public class TestJwtToken
{
    /**
     * @author Thomas Freese
     */
    final class HttpHeaderInterceptor implements ClientHttpRequestInterceptor
    {
        /**
        *
        */
        private final String name;

        /**
        *
        */
        private final String value;

        /**
         * Creates a new {@link HttpHeaderInterceptor} instance.
         *
         * @param name the header name to populate. Cannot be null or empty.
         * @param value the header value to populate. Cannot be null or empty.
         */
        HttpHeaderInterceptor(final String name, final String value)
        {
            super();

            this.name = Objects.requireNonNull(name, "name required");
            this.value = Objects.requireNonNull(value, "value required");
        }

        /**
         * @see org.springframework.http.client.ClientHttpRequestInterceptor#intercept(org.springframework.http.HttpRequest, byte[],
         *      org.springframework.http.client.ClientHttpRequestExecution)
         */
        @Override
        public ClientHttpResponse intercept(final HttpRequest request, final byte[] body, final ClientHttpRequestExecution execution) throws IOException
        {
            request.getHeaders().add(this.name, this.value);

            return execution.execute(request, body);
        }
    }

    /**
     * @author Thomas Freese
     */
    private class NoOpResponseErrorHandler extends DefaultResponseErrorHandler
    {
        /**
         * Erstellt ein neues {@link NoOpResponseErrorHandler} Object.
         */
        private NoOpResponseErrorHandler()
        {
            super();
        }

        /**
         * @see org.springframework.web.client.DefaultResponseErrorHandler#handleError(org.springframework.http.client.ClientHttpResponse)
         */
        @Override
        public void handleError(final ClientHttpResponse response) throws IOException
        {
            // Das Auslesen des Responses ist nur einmal möglich !
            // Das für bei den Tests zu Fehlern.

            // RestClientResponseException exception =
            // new RestClientResponseException("Server Error: [" + response.getRawStatusCode() + "]" + " " + response.getStatusText(),
            // response.getRawStatusCode(), response.getStatusText(), response.getHeaders(), getResponseBody(response), getCharset(response));
            //
            // System.err.println(exception);
            // // exception.printStackTrace();
            //
            // try
            // {
            // ApiError apiError = TestRestApi.this.objectMapper.readValue(exception.getResponseBodyAsByteArray(), ApiError.class);
            // // exception.setStackTrace(apiError.getStackTrace());
            // System.err.println(apiError);
            // }
            // catch (Exception ex)
            // {
            // }
        }
    }

    /**
    *
    */
    @LocalServerPort
    private int localServerPort = 0;

    /**
     *
     */
    @Resource
    private RestTemplateBuilder restTemplateBuilder = null;

    /**
     *
     */
    @Resource
    private JwtTokenProvider tokenProvider = null;

    /**
     * Erstellt ein neues {@link TestJwtToken} Object.
     */
    public TestJwtToken()
    {
        super();
    }

    /**
     * @throws Exception Falls was schief geht.
     */
    @BeforeEach
    public void beforeTest() throws Exception
    {
        String rootUri = "http://localhost:" + this.localServerPort;

        // @formatter:off
        this.restTemplateBuilder = this.restTemplateBuilder
                .rootUri(rootUri)
                .errorHandler(new NoOpResponseErrorHandler())
        ;
        // @formatter:on
    }

    /**
     * @throws Exception Falls was schief geht.
     */
    @Test
    public void test000Fail() throws Exception
    {
        // @formatter:off
        RestTemplate restTemplate = this.restTemplateBuilder
//                .interceptors(
//                        //new HttpHeaderInterceptor("Authorization", "Bearer " + this.tokenProvider.createToken("user", "pw")),
//                        new HttpHeaderInterceptor("Accept", MediaType.APPLICATION_JSON_VALUE))
                .defaultHeader("Accept", MediaType.APPLICATION_JSON_VALUE)
                .build();
        // @formatter:on

        ResponseEntity<String> responseEntity = restTemplate.getForEntity("/jwt/users/me", String.class);

        Assertions.assertEquals(MediaType.APPLICATION_JSON_VALUE, responseEntity.getHeaders().getFirst("Content-Type"));
        Assertions.assertNotNull(responseEntity.getBody());
        Assertions.assertEquals(responseEntity.getStatusCode(), HttpStatus.FORBIDDEN);

        System.out.println(JsonFormatter.prettyPrint(responseEntity.getBody()));
    }

    /**
     * @throws Exception Falls was schief geht.
     */
    @Test
    public void test010Me() throws Exception
    {
        // @formatter:off
        RestTemplate restTemplate = this.restTemplateBuilder
//                .interceptors(
//                        new HttpHeaderInterceptor("Authorization", "Bearer " + this.tokenProvider.createToken("user", "pw")),
//                        new HttpHeaderInterceptor("Accept", MediaType.APPLICATION_JSON_VALUE))
                .defaultHeader("Authorization", "Bearer " + this.tokenProvider.createToken("user", "pw"))
                .defaultHeader("Accept", MediaType.APPLICATION_JSON_VALUE)
                .build();
        // @formatter:on

        ResponseEntity<String> responseEntity = restTemplate.getForEntity("/jwt/users/me", String.class);

        Assertions.assertEquals(MediaType.APPLICATION_JSON_VALUE, responseEntity.getHeaders().getFirst("Content-Type"));
        Assertions.assertNotNull(responseEntity.getBody());
        Assertions.assertEquals(responseEntity.getStatusCode(), HttpStatus.OK);

        System.out.println(JsonFormatter.prettyPrint(responseEntity.getBody()));
        //
        // String status = JsonPath.parse(responseEntity.getBody()).read("$.status");
        // Assert.assertEquals("UP", status);
    }

    /**
     * @throws Exception Falls was schief geht.
     */
    @Test
    public void test011Me() throws Exception
    {
        // @formatter:off
        RestTemplate restTemplate = this.restTemplateBuilder
//                .interceptors(
//                        new HttpHeaderInterceptor("Authorization", "Bearer " + this.tokenProvider.createToken("user", "pw")),
//                        new HttpHeaderInterceptor("Accept", MediaType.APPLICATION_JSON_VALUE))
                .defaultHeader("Authorization", "Bearer " + this.tokenProvider.createToken("user", "pw"))
                .defaultHeader("Accept", MediaType.APPLICATION_JSON_VALUE)
                .build();
        // @formatter:on

        ResponseEntity<String> responseEntity = restTemplate.getForEntity("/jwt/users/me", String.class);

        Assertions.assertEquals(MediaType.APPLICATION_JSON_VALUE, responseEntity.getHeaders().getFirst("Content-Type"));
        Assertions.assertNotNull(responseEntity.getBody());
        Assertions.assertEquals(responseEntity.getStatusCode(), HttpStatus.OK);

        System.out.println(JsonFormatter.prettyPrint(responseEntity.getBody()));
    }
}
