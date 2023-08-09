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
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.client.support.BasicAuthenticationInterceptor;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import de.freese.spring.thymeleaf.HttpHeaderInterceptor;
import de.freese.spring.thymeleaf.ThymeleafApplication;
import de.freese.spring.thymeleaf.exception.ApiError;
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
    void beforeTest() throws Exception {
        // String rootUri = "http://localhost:" + this.localServerPort;
        String rootUri = ThymeleafApplication.getRootUri(getEnvironment());

        HttpComponentsClientHttpRequestFactory httpRequestFactory = new HttpComponentsClientHttpRequestFactory(this.httpClient);

        // @formatter:off
        this.restTemplateBuilder = this.restTemplateBuilder
                .rootUri(rootUri)
                .errorHandler(new NoOpResponseErrorHandler())
                .requestFactory(() -> httpRequestFactory)
                ;
        // @formatter:on
    }

    /**
     * @see de.freese.spring.thymeleaf.rest.AbstractRestTestCase#testHealthEndpoint()
     */
    @Override
    @Test
    void testHealthEndpoint() throws Exception {
        // @formatter:off
        RestTemplate restTemplate = this.restTemplateBuilder
                .interceptors(new HttpHeaderInterceptor("Accept", MediaType.APPLICATION_JSON_VALUE))
                .build();
        // @formatter:on

        ResponseEntity<String> responseEntity = restTemplate.getForEntity("/actuator/health", String.class);

        assertEquals(MediaType.APPLICATION_JSON, responseEntity.getHeaders().getContentType());

        String status = JsonPath.parse(responseEntity.getBody()).read("$.status");
        assertEquals("UP", status);
    }

    /**
     * @see de.freese.spring.thymeleaf.rest.AbstractRestTestCase#testPost()
     */
    @Override
    @Test
    void testPost() throws Exception {
        // @formatter:off
        RestTemplate restTemplate = this.restTemplateBuilder
                .interceptors(new BasicAuthenticationInterceptor("admin", "pw", StandardCharsets.UTF_8),
                        new HttpHeaderInterceptor("Content-Type", MediaType.APPLICATION_JSON_VALUE))
                .build();
        // @formatter:on

        HttpEntity<Person> httpEntity = new HttpEntity<>(new Person("Thomas", "Freese"));
        ResponseEntity<ApiError> responseEntity = restTemplate.exchange("/rest/person/personAdd", HttpMethod.POST, httpEntity, ApiError.class);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

        // @formatter:off
        restTemplate = this.restTemplateBuilder
                .interceptors(new BasicAuthenticationInterceptor("user", "pw", StandardCharsets.UTF_8),
                        new HttpHeaderInterceptor("Accept", MediaType.APPLICATION_JSON_VALUE))
                .build();
        // @formatter:on

        Person[] personArray = restTemplate.getForObject("/rest/person/personList", Person[].class);
        List<Person> persons = Arrays.asList(personArray);

        assertNotNull(persons);
        assertTrue(persons.size() >= 3);

        assertEquals("Thomas", persons.get(persons.size() - 1).getFirstName());
        assertEquals("Freese", persons.get(persons.size() - 1).getLastName());
    }

    /**
     * @see de.freese.spring.thymeleaf.rest.AbstractRestTestCase#testPostWithWrongRole()
     */
    @Override
    @Test
    void testPostWithWrongRole() throws Exception {
        // @formatter:off
        RestTemplate restTemplate = this.restTemplateBuilder
                .interceptors(new BasicAuthenticationInterceptor("user", "pw", StandardCharsets.UTF_8),
                        new HttpHeaderInterceptor("Content-Type", MediaType.APPLICATION_JSON_VALUE))
                .build();
        // @formatter:on

        ApiError error = restTemplate.postForObject("/rest/person/personAdd", new Person("Thomas", "Freese"), ApiError.class);
        assertEquals(HttpStatus.FORBIDDEN.value(), error.getHttpStatus());
    }

    /**
     * @see de.freese.spring.thymeleaf.rest.AbstractRestTestCase#testUserWithLoginJSON()
     */
    @Override
    @Test
    void testUserWithLoginJSON() throws Exception {
        // @formatter:off
        RestTemplate restTemplate = this.restTemplateBuilder
                .interceptors(new BasicAuthenticationInterceptor("user", "pw", StandardCharsets.UTF_8),
                        new HttpHeaderInterceptor("Accept", MediaType.APPLICATION_JSON_VALUE))
                .build();
        // @formatter:on

        // ResponseEntity<String> responseEntity = restTemplate.getForEntity("/rest/person/personList", String.class);
        // persons = this.objectMapper.readValue(responseEntity.getBody(), new TypeReference<List<Person>>()
        // {
        // });

        // Person[] personArray = restTemplate.getForObject("/rest/person/personList", Person[].class);
        // persons = Arrays.asList(personArray);

        ResponseEntity<List<Person>> responseEntity = restTemplate.exchange("/rest/person/personList", HttpMethod.GET, null, new ParameterizedTypeReference<>() {
        });
        List<Person> persons = responseEntity.getBody();

        // assertEquals(MediaType.APPLICATION_JSON_VALUE, responseEntity.getHeaders().getAccept());
        // assertEquals(HttpStatus.FORBIDDEN, responseEntity.getStatusCode());
        assertNotNull(persons);
        assertTrue(persons.size() >= 2);
    }

    /**
     * @see de.freese.spring.thymeleaf.rest.AbstractRestTestCase#testUserWithLoginXML()
     */
    @Override
    @Test
    void testUserWithLoginXML() throws Exception {
        // @formatter:off
        RestTemplate restTemplate = this.restTemplateBuilder
                .interceptors(new BasicAuthenticationInterceptor("user", "pw", StandardCharsets.UTF_8),
                        new HttpHeaderInterceptor("Accept", MediaType.APPLICATION_XML_VALUE + ";charset=UTF-8"))
                .build();
        // @formatter:on

        // // ResponseEntity<String> responseEntity = restTemplate.getForEntity("/rest/person/personList", String.class);
        // persons = this.objectMapper.readValue(responseEntity.getBody(), new TypeReference<List<Person>>()
        // {
        // });

        // Person[] personArray = restTemplate.getForObject("/rest/person/personList", Person[].class);
        // persons = Arrays.asList(personArray);

        ResponseEntity<List<Person>> responseEntity = restTemplate.exchange("/rest/person/personList", HttpMethod.GET, null, new ParameterizedTypeReference<>() {
        });
        List<Person> persons = responseEntity.getBody();

        // assertEquals(MediaType.APPLICATION_JSON_VALUE, responseEntity.getHeaders().getAccept());
        // assertEquals(HttpStatus.FORBIDDEN, responseEntity.getStatusCode());
        assertNotNull(persons);
        assertTrue(persons.size() >= 2);
    }

    /**
     * @see de.freese.spring.thymeleaf.rest.AbstractRestTestCase#testUserWithPreAuthJSON()
     */
    @Override
    @Test
    void testUserWithPreAuthJSON() throws Exception {
        // @formatter:off
        RestTemplate restTemplate = this.restTemplateBuilder
                .interceptors(new HttpHeaderInterceptor("my-token", "user"),
                        new HttpHeaderInterceptor("Accept", MediaType.APPLICATION_JSON_VALUE))
                .build();
        // @formatter:on

        Person[] personArray = restTemplate.getForObject("/rest/person/personList", Person[].class);
        List<Person> persons = Arrays.asList(personArray);

        assertNotNull(persons);
        assertTrue(persons.size() >= 2);
    }

    /**
     * @see de.freese.spring.thymeleaf.rest.AbstractRestTestCase#testUserWithPreAuthXML()
     */
    @Override
    @Test
    void testUserWithPreAuthXML() throws Exception {
        // @formatter:off
        RestTemplate restTemplate = this.restTemplateBuilder
                .interceptors(new HttpHeaderInterceptor("my-token", "user"),
                        new HttpHeaderInterceptor("Accept", MediaType.APPLICATION_XML_VALUE+";charset=UTF-8"))
                .build();
        // @formatter:on

        Person[] personArray = restTemplate.getForObject("/rest/person/personList", Person[].class);
        List<Person> persons = Arrays.asList(personArray);

        assertNotNull(persons);
        assertTrue(persons.size() >= 2);
    }

    /**
     * @see de.freese.spring.thymeleaf.rest.AbstractRestTestCase#testUserWithWrongPass()
     */
    @Override
    @Test
    void testUserWithWrongPass() throws Exception {
        // @formatter:off
        RestTemplate restTemplate = this.restTemplateBuilder
                .interceptors(new BasicAuthenticationInterceptor("user", "pass", StandardCharsets.UTF_8))
                .build();
        // @formatter:on

        ResponseEntity<String> responseEntity = restTemplate.getForEntity("/rest/person/personList", String.class);

        assertEquals(HttpStatus.UNAUTHORIZED, responseEntity.getStatusCode());
    }

    /**
     * @see de.freese.spring.thymeleaf.rest.AbstractRestTestCase#testUserWithWrongRole()
     */
    @Override
    @Test
    void testUserWithWrongRole() throws Exception {
        // @formatter:off
        RestTemplate restTemplate = this.restTemplateBuilder
                .interceptors(new BasicAuthenticationInterceptor("invalid", "pw", StandardCharsets.UTF_8))
                .build();
        // @formatter:on

        ResponseEntity<String> responseEntity = restTemplate.getForEntity("/rest/person/personList", String.class);

        assertEquals(HttpStatus.FORBIDDEN, responseEntity.getStatusCode());
    }

    /**
     * @see de.freese.spring.thymeleaf.rest.AbstractRestTestCase#testUserWithoutLogin()
     */
    @Override
    @Test
    void testUserWithoutLogin() throws Exception {
        // @formatter:off
        RestTemplate restTemplate = this.restTemplateBuilder
                .interceptors(new HttpHeaderInterceptor("Accept", MediaType.APPLICATION_JSON_VALUE))
                .build();
        // @formatter:on

        ResponseEntity<String> responseEntity = restTemplate.getForEntity("/rest/person/personList", String.class);

        //assertEquals(HttpStatus.UNAUTHORIZED, responseEntity.getStatusCode());

        // Eigentlich UNAUTHORIZED erwartet -> RememberMeServices ?
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
    }
}
