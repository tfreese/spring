// Created: 07.09.2018
package de.freese.spring.jwt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;

import javax.annotation.Resource;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.jayway.jsonpath.internal.JsonFormatter;

import de.freese.spring.jwt.token.JwtTokenProvider;

/**
 * @author Thomas Freese
 */
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT, classes = JwtAuthorisationApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.MethodName.class)
class TestJwtToken
{
    /**
     * @author Thomas Freese
     */
    private static class NoOpResponseErrorHandler extends DefaultResponseErrorHandler
    {
        /**
         * @see org.springframework.web.client.DefaultResponseErrorHandler#handleError(org.springframework.http.client.ClientHttpResponse)
         */
        @Override
        public void handleError(final ClientHttpResponse response) throws IOException
        {
            // Das Auslesen des Responses ist nur einmal möglich !
            // Das führt bei den Tests zu Fehlern, da für die Asserts ein 2. x auf den Response zugegriffen werden muss.

            // RestClientResponseException exception =
            // new RestClientResponseException("Server Error: [" + response.getRawStatusCode() + "]" + " " + response.getStatusText(),
            // response.getRawStatusCode(), response.getStatusText(), response.getHeaders(), getResponseBody(response), getCharset(response));
            //
            // System.err.println(exception);

            // try
            // {
            // ApiError apiError = TestRestApi.this.objectMapper.readValue(exception.getResponseBodyAsByteArray(), ApiError.class);
            // // exception.setStackTrace(apiError.getStackTrace());
            // System.err.println(apiError);
            // }
            // catch (Exception ex)
            // {
            // // Empty
            // }
        }
    }

    /**
    *
    */
    @LocalServerPort
    private int localServerPort;
    /**
     *
     */
    @Resource
    private RestTemplateBuilder restTemplateBuilder;
    /**
     *
     */
    @Resource
    private JwtTokenProvider tokenProvider;

    /**
     * @throws Exception Falls was schief geht.
     */
    @BeforeEach
    void beforeEach() throws Exception
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
    void testFailNoLogin() throws Exception
    {
        // @formatter:off
        RestTemplate restTemplate = this.restTemplateBuilder
//                .interceptors(
//                        //new HttpHeaderInterceptor("Authorization", "Bearer " + this.tokenProvider.createToken("user", "pass")),
//                        new HttpHeaderInterceptor("Accept", MediaType.APPLICATION_JSON_VALUE))
                .defaultHeader("Accept", MediaType.APPLICATION_JSON_VALUE)
                .build()
                ;
        // @formatter:on

        ResponseEntity<String> responseEntity = restTemplate.getForEntity("/jwt/users/me", String.class);

        assertEquals(HttpStatus.FORBIDDEN, responseEntity.getStatusCode());

        if (responseEntity.hasBody())
        {
            System.out.printf("%nFail: %s%n", JsonFormatter.prettyPrint(responseEntity.getBody()));
        }
    }

    /**
     * @throws Exception Falls was schief geht.
     */
    @Test
    void testFailWrongPass() throws Exception
    {
        // @formatter:off
        RestTemplate restTemplate = this.restTemplateBuilder
                .defaultHeader("Authorization", "Bearer " + this.tokenProvider.createToken("admin", "pas"))
                .defaultHeader("Accept", MediaType.APPLICATION_JSON_VALUE)
                .build()
                ;
        // @formatter:on

        ResponseEntity<String> responseEntity = restTemplate.getForEntity("/jwt/users/me", String.class);

        assertEquals(HttpStatus.UNAUTHORIZED, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());

        // UTF-8 kommt aus WebSecurityComponentsConfig.RestAuthenticationEntryPoint.commence.
        assertEquals(MediaType.APPLICATION_JSON + ";charset=UTF-8", responseEntity.getHeaders().getFirst("Content-Type"));

        System.out.printf("%nWrong Pass: %s%n", responseEntity.getBody());
    }

    /**
     * @throws Exception Falls was schief geht.
     */
    @Test
    void testLoginAdmin() throws Exception
    {
        // @formatter:off
        RestTemplate restTemplate = this.restTemplateBuilder
                .defaultHeader("Accept", MediaType.APPLICATION_JSON_VALUE)
                .build()
                ;
        // @formatter:on

        String uri = UriComponentsBuilder.fromPath("/jwt/users/login").queryParam("username", "admin").queryParam("password", "pass").toUriString();
        ResponseEntity<String> responseEntity = restTemplate.getForEntity(uri, String.class);

        Assertions.assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        Assertions.assertNotNull(responseEntity.getBody());
        Assertions.assertEquals(MediaType.TEXT_PLAIN_VALUE + ";charset=UTF-8", responseEntity.getHeaders().getFirst("Content-Type"));

        System.out.printf("%nLogin Admin: %s%n", responseEntity.getBody());
    }

    /**
     * @throws Exception Falls was schief geht.
     */
    @Test
    void testLoginUser() throws Exception
    {
        // @formatter:off
        RestTemplate restTemplate = this.restTemplateBuilder
                .defaultHeader("Accept", MediaType.APPLICATION_JSON_VALUE)
                .build()
                ;
        // @formatter:on

        String uri = UriComponentsBuilder.fromPath("/jwt/users/login").queryParam("username", "user").queryParam("password", "pass").toUriString();
        ResponseEntity<String> responseEntity = restTemplate.getForEntity(uri, String.class);

        Assertions.assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        Assertions.assertNotNull(responseEntity.getBody());
        Assertions.assertEquals(MediaType.TEXT_PLAIN_VALUE + ";charset=UTF-8", responseEntity.getHeaders().getFirst("Content-Type"));

        System.out.printf("%nLogin User: %s%n", responseEntity.getBody());
    }

    /**
     * @throws Exception Falls was schief geht.
     */
    @Test
    void testMeAdmin() throws Exception
    {
        // @formatter:off
        RestTemplate restTemplate = this.restTemplateBuilder
                .defaultHeader("Authorization", "Bearer " + this.tokenProvider.createToken("admin", "pass"))
                .defaultHeader("Accept", MediaType.APPLICATION_JSON_VALUE)
                .build()
                ;
        // @formatter:on

        ResponseEntity<String> responseEntity = restTemplate.getForEntity("/jwt/users/me", String.class);

        Assertions.assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        Assertions.assertNotNull(responseEntity.getBody());
        Assertions.assertEquals(MediaType.APPLICATION_JSON_VALUE, responseEntity.getHeaders().getFirst("Content-Type"));

        System.out.printf("%nMe Admin: %s%n", JsonFormatter.prettyPrint(responseEntity.getBody()));
    }

    /**
     * @throws Exception Falls was schief geht.
     */
    @Test
    void testMeUser() throws Exception
    {
        // @formatter:off
        RestTemplate restTemplate = this.restTemplateBuilder
                .defaultHeader("Authorization", "Bearer " + this.tokenProvider.createToken("user", "pass"))
                .defaultHeader("Accept", MediaType.APPLICATION_JSON_VALUE)
                .build()
                ;
        // @formatter:on

        ResponseEntity<String> responseEntity = restTemplate.getForEntity("/jwt/users/me", String.class);

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());
        assertEquals(MediaType.APPLICATION_JSON_VALUE, responseEntity.getHeaders().getFirst("Content-Type"));

        System.out.printf("%nMe User: %s%n", JsonFormatter.prettyPrint(responseEntity.getBody()));
    }

    /**
     * @throws Exception Falls was schief geht.
     */
    @Test
    void testSearchAdmin() throws Exception
    {
        // @formatter:off
        RestTemplate restTemplate = this.restTemplateBuilder
                .defaultHeader("Authorization", "Bearer " + this.tokenProvider.createToken("admin", "pass"))
                .defaultHeader("Accept", MediaType.APPLICATION_JSON_VALUE)
                .build()
                ;
        // @formatter:on

        ResponseEntity<String> responseEntity = restTemplate.getForEntity("/jwt/users/search/user", String.class);

        Assertions.assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        Assertions.assertNotNull(responseEntity.getBody());
        Assertions.assertEquals(MediaType.APPLICATION_JSON_VALUE, responseEntity.getHeaders().getFirst("Content-Type"));

        System.out.printf("%nSearch Admin: %s%n", JsonFormatter.prettyPrint(responseEntity.getBody()));
    }

    /**
     * @throws Exception Falls was schief geht.
     */
    @Test
    void testSearchUser() throws Exception
    {
        // @formatter:off
        RestTemplate restTemplate = this.restTemplateBuilder
                .defaultHeader("Authorization", "Bearer " + this.tokenProvider.createToken("user", "pass"))
                .defaultHeader("Accept", MediaType.APPLICATION_JSON_VALUE)
                .build()
                ;
        // @formatter:on

        ResponseEntity<String> responseEntity = restTemplate.getForEntity("/jwt/users/search/user", String.class);

        Assertions.assertEquals(HttpStatus.FORBIDDEN, responseEntity.getStatusCode());

        System.out.printf("%nSearch User: %s%n", responseEntity.getBody());
    }
}
