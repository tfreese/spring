/**
 * Created: 07.09.2018
 */

package org.spring.oauth.jwt;

import java.io.IOException;
import java.util.Objects;
import javax.annotation.Resource;
import org.junit.Assert;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
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
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;
import com.jayway.jsonpath.internal.JsonFormatter;

/**
 * @author Thomas Freese
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT, classes =
{
        OauthJwtAuthorisationApplication.class
})
@AutoConfigureMockMvc
@ActiveProfiles("test")
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TestOauthJwtToken
{
    /**
     * @author Thomas Freese
     */
    private final class HttpHeaderInterceptor implements ClientHttpRequestInterceptor
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
        public HttpHeaderInterceptor(final String name, final String value)
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
     * Erstellt ein neues {@link TestOauthJwtToken} Object.
     */
    public TestOauthJwtToken()
    {
        super();
    }

    /**
     * @throws Exception Falls was schief geht.
     */
    @Before
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
                .interceptors(
                        //new HttpHeaderInterceptor("Authorization", "Bearer " + this.tokenProvider.createToken("user", "pw")),
                        new HttpHeaderInterceptor("Accept", MediaType.APPLICATION_JSON_UTF8_VALUE))
                .build();
        // @formatter:on

        ResponseEntity<String> responseEntity = restTemplate.getForEntity("/auth/rest/me", String.class);

        Assert.assertEquals(MediaType.APPLICATION_JSON_UTF8_VALUE, responseEntity.getHeaders().getFirst("Content-Type"));
        Assert.assertNotNull(responseEntity.getBody());
        Assert.assertEquals(responseEntity.getStatusCode(), HttpStatus.FORBIDDEN);

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
                .interceptors(
                        new HttpHeaderInterceptor("Accept", MediaType.APPLICATION_JSON_UTF8_VALUE))
                .build();
        // @formatter:on

        ResponseEntity<String> responseEntity = restTemplate.getForEntity("/auth/rest/message", String.class);

        Assert.assertEquals(MediaType.APPLICATION_JSON_UTF8_VALUE, responseEntity.getHeaders().getFirst("Content-Type"));
        Assert.assertNotNull(responseEntity.getBody());

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
                .interceptors(
                        new HttpHeaderInterceptor("Accept", MediaType.APPLICATION_JSON_UTF8_VALUE))
                .build();
        // @formatter:on

        ResponseEntity<String> responseEntity = restTemplate.getForEntity("/auth/rest/message", String.class);

        Assert.assertEquals(MediaType.APPLICATION_JSON_UTF8_VALUE, responseEntity.getHeaders().getFirst("Content-Type"));
        Assert.assertNotNull(responseEntity.getBody());

        System.out.println(JsonFormatter.prettyPrint(responseEntity.getBody()));
    }
}
