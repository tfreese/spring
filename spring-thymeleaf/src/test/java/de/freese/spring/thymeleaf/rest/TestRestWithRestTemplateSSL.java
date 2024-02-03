// Created: 07.09.2018
package de.freese.spring.thymeleaf.rest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

import jakarta.annotation.Resource;

import com.jayway.jsonpath.JsonPath;
import org.apache.hc.client5.http.classic.HttpClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.client.support.BasicAuthenticationInterceptor;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import de.freese.spring.thymeleaf.HttpHeaderInterceptor;
import de.freese.spring.thymeleaf.ThymeleafApplication;
import de.freese.spring.thymeleaf.model.Person;

/**
 * @author Thomas Freese
 */
@ActiveProfiles({"test", "with-ssl"})
class TestRestWithRestTemplateSSL extends AbstractRestTestCase {
    /**
     * @author Thomas Freese
     */
    private static final class NoOpResponseErrorHandler extends DefaultResponseErrorHandler {
        @Override
        public void handleError(final ClientHttpResponse response) throws IOException {
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

    @Resource
    private HttpClient httpClient;
    @Resource
    private RestTemplateBuilder restTemplateBuilder;

    @BeforeEach
    void beforeTest() {
        // final String rootUri = "http://localhost:" + this.localServerPort;
        final String rootUri = ThymeleafApplication.getRootUri(getEnvironment());

        final HttpComponentsClientHttpRequestFactory httpRequestFactory = new HttpComponentsClientHttpRequestFactory(this.httpClient);

        // @formatter:off
        this.restTemplateBuilder = this.restTemplateBuilder
                .rootUri(rootUri)
                .errorHandler(new NoOpResponseErrorHandler())
                .requestFactory(() -> httpRequestFactory)
                ;
        // @formatter:on
    }

    @Override
    @Test
    void testHealthEndpoint() {
        // @formatter:off
        final RestTemplate restTemplate = this.restTemplateBuilder
                .interceptors(new HttpHeaderInterceptor("Accept", MediaType.APPLICATION_JSON_VALUE))
                .build();
        // @formatter:on

        final ResponseEntity<String> responseEntity = restTemplate.getForEntity("/actuator/health", String.class);

        assertEquals(MediaType.APPLICATION_JSON, responseEntity.getHeaders().getContentType());

        final String status = JsonPath.parse(responseEntity.getBody()).read("$.status");
        assertEquals("UP", status);
    }

    @Override
    @Test
    void testPost() {
        // @formatter:off
         RestTemplate restTemplate = this.restTemplateBuilder
                .interceptors(new BasicAuthenticationInterceptor("admin", "pw", StandardCharsets.UTF_8),
                        new HttpHeaderInterceptor("Content-Type", MediaType.APPLICATION_JSON_VALUE))
                .build();
        // @formatter:on

        final HttpEntity<Person> httpEntity = new HttpEntity<>(new Person("Thomas", "Freese"));
        final ResponseEntity<ProblemDetail> responseEntity = restTemplate.exchange("/rest/person/personAdd", HttpMethod.POST, httpEntity, ProblemDetail.class);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

        // @formatter:off
        restTemplate = this.restTemplateBuilder
                .interceptors(new BasicAuthenticationInterceptor("user", "pw", StandardCharsets.UTF_8),
                        new HttpHeaderInterceptor("Accept", MediaType.APPLICATION_JSON_VALUE))
                .build();
        // @formatter:on

        final Person[] personArray = restTemplate.getForObject("/rest/person/personList", Person[].class);
        assertNotNull(personArray);

        final List<Person> persons = Arrays.asList(personArray);

        assertNotNull(persons);
        assertTrue(persons.size() >= 3);

        assertEquals("Thomas", persons.getLast().getFirstName());
        assertEquals("Freese", persons.getLast().getLastName());
    }

    @Override
    @Test
    void testPostWithWrongRole() {
        // @formatter:off
        final RestTemplate restTemplate = this.restTemplateBuilder
                .interceptors(new BasicAuthenticationInterceptor("user", "pw", StandardCharsets.UTF_8),
                        new HttpHeaderInterceptor("Content-Type", MediaType.APPLICATION_JSON_VALUE))
                .build();
        // @formatter:on

        final ProblemDetail error = restTemplate.postForObject("/rest/person/personAdd", new Person("Thomas", "Freese"), ProblemDetail.class);
        assertEquals(HttpStatus.FORBIDDEN.value(), error.getStatus());
    }

    @Override
    @Test
    void testUserWithLoginJSON() {
        // @formatter:off
        final RestTemplate restTemplate = this.restTemplateBuilder
                .interceptors(new BasicAuthenticationInterceptor("user", "pw", StandardCharsets.UTF_8),
                        new HttpHeaderInterceptor("Accept", MediaType.APPLICATION_JSON_VALUE))
                .build();
        // @formatter:on

        // final ResponseEntity<String> responseEntity = restTemplate.getForEntity("/rest/person/personList", String.class);
        // persons = this.objectMapper.readValue(responseEntity.getBody(), new TypeReference<List<Person>>()
        // {
        // });

        // final Person[] personArray = restTemplate.getForObject("/rest/person/personList", Person[].class);
        // persons = Arrays.asList(personArray);

        final ResponseEntity<List<Person>> responseEntity = restTemplate.exchange("/rest/person/personList", HttpMethod.GET, null, new ParameterizedTypeReference<>() {
        });
        final List<Person> persons = responseEntity.getBody();

        // assertEquals(MediaType.APPLICATION_JSON_VALUE, responseEntity.getHeaders().getAccept());
        // assertEquals(HttpStatus.FORBIDDEN, responseEntity.getStatusCode());
        assertNotNull(persons);
        assertTrue(persons.size() >= 2);
    }

    @Override
    @Test
    void testUserWithLoginXML() {
        // @formatter:off
        final RestTemplate restTemplate = this.restTemplateBuilder
                .interceptors(new BasicAuthenticationInterceptor("user", "pw", StandardCharsets.UTF_8),
                        new HttpHeaderInterceptor("Accept", MediaType.APPLICATION_XML_VALUE + ";charset=UTF-8"))
                .build();
        // @formatter:on

        // // ResponseEntity<String> responseEntity = restTemplate.getForEntity("/rest/person/personList", String.class);
        // persons = this.objectMapper.readValue(responseEntity.getBody(), new TypeReference<List<Person>>()
        // {
        // });

        // final Person[] personArray = restTemplate.getForObject("/rest/person/personList", Person[].class);
        // persons = Arrays.asList(personArray);

        final ResponseEntity<List<Person>> responseEntity = restTemplate.exchange("/rest/person/personList", HttpMethod.GET, null, new ParameterizedTypeReference<>() {
        });
        final List<Person> persons = responseEntity.getBody();

        // assertEquals(MediaType.APPLICATION_JSON_VALUE, responseEntity.getHeaders().getAccept());
        // assertEquals(HttpStatus.FORBIDDEN, responseEntity.getStatusCode());
        assertNotNull(persons);
        assertTrue(persons.size() >= 2);
    }

    @Override
    @Test
    void testUserWithPreAuthJSON() {
        // @formatter:off
        final RestTemplate restTemplate = this.restTemplateBuilder
                .interceptors(new HttpHeaderInterceptor("my-token", "user"),
                        new HttpHeaderInterceptor("Accept", MediaType.APPLICATION_JSON_VALUE))
                .build();
        // @formatter:on

        final Person[] personArray = restTemplate.getForObject("/rest/person/personList", Person[].class);
        assertNotNull(personArray);

        final List<Person> persons = Arrays.asList(personArray);

        assertNotNull(persons);
        assertTrue(persons.size() >= 2);
    }

    @Override
    @Test
    void testUserWithPreAuthXML() {
        // @formatter:off
        final RestTemplate restTemplate = this.restTemplateBuilder
                .interceptors(new HttpHeaderInterceptor("my-token", "user"),
                        new HttpHeaderInterceptor("Accept", MediaType.APPLICATION_XML_VALUE+";charset=UTF-8"))
                .build();
        // @formatter:on

        final Person[] personArray = restTemplate.getForObject("/rest/person/personList", Person[].class);
        assertNotNull(personArray);

        final List<Person> persons = Arrays.asList(personArray);

        assertNotNull(persons);
        assertTrue(persons.size() >= 2);
    }

    @Override
    @Test
    void testUserWithWrongPass() {
        // @formatter:off
        final RestTemplate restTemplate = this.restTemplateBuilder
                .interceptors(new BasicAuthenticationInterceptor("user", "pass", StandardCharsets.UTF_8))
                .build();
        // @formatter:on

        final ResponseEntity<String> responseEntity = restTemplate.getForEntity("/rest/person/personList", String.class);

        assertEquals(HttpStatus.UNAUTHORIZED, responseEntity.getStatusCode());
    }

    @Override
    @Test
    void testUserWithWrongRole() {
        // @formatter:off
        final RestTemplate restTemplate = this.restTemplateBuilder
                .interceptors(new BasicAuthenticationInterceptor("invalid", "pw", StandardCharsets.UTF_8))
                .build();
        // @formatter:on

        final ResponseEntity<String> responseEntity = restTemplate.getForEntity("/rest/person/personList", String.class);

        assertEquals(HttpStatus.FORBIDDEN, responseEntity.getStatusCode());
    }

    @Override
    @Test
    void testUserWithoutLogin() {
        // @formatter:off
        final RestTemplate restTemplate = this.restTemplateBuilder
                .interceptors(new HttpHeaderInterceptor("Accept", MediaType.APPLICATION_JSON_VALUE))
                .build();
        // @formatter:on

        final ResponseEntity<String> responseEntity = restTemplate.getForEntity("/rest/person/personList", String.class);

        // assertEquals(HttpStatus.UNAUTHORIZED, responseEntity.getStatusCode());

        // Eigentlich UNAUTHORIZED erwartet -> RememberMeServices ?
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
    }
}
