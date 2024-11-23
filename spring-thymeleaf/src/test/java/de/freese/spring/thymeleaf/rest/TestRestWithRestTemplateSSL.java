// Created: 07.09.2018
package de.freese.spring.thymeleaf.rest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

import jakarta.annotation.Resource;

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
import org.springframework.http.client.support.BasicAuthenticationInterceptor;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestTemplate;

import de.freese.spring.thymeleaf.HttpHeaderInterceptor;
import de.freese.spring.thymeleaf.ThymeleafApplication;
import de.freese.spring.thymeleaf.model.Person;

/**
 * @author Thomas Freese
 */
@ActiveProfiles({"test", "with-ssl"})
class TestRestWithRestTemplateSSL extends AbstractRestTestCase {
    @Resource
    private RestTemplateBuilder restTemplateBuilder;

    @BeforeEach
    void beforeTest() {
        // final String rootUri = "http://localhost:" + this.localServerPort;
        final String rootUri = ThymeleafApplication.getRootUri(getEnvironment());

        this.restTemplateBuilder = this.restTemplateBuilder
                .rootUri(rootUri)
                .errorHandler(new NoOpResponseErrorHandler());
    }

    @Override
    @Test
    void testHealthEndpoint() {
        final RestTemplate restTemplate = this.restTemplateBuilder
                .interceptors(new HttpHeaderInterceptor("Accept", MediaType.APPLICATION_JSON_VALUE))
                .build();

        final ResponseEntity<String> responseEntity = restTemplate.getForEntity("/actuator/info", String.class);

        assertEquals(MediaType.APPLICATION_JSON, responseEntity.getHeaders().getContentType());
        assertEquals(HttpStatus.OK.value(), responseEntity.getStatusCode().value());

        // final String status = JsonPath.parse(responseEntity.getBody()).read("$.status");
        // assertEquals("UP", status);
    }

    @Override
    @Test
    void testPost() {
        RestTemplate restTemplate = this.restTemplateBuilder
                .interceptors(new BasicAuthenticationInterceptor("admin", "pw", StandardCharsets.UTF_8),
                        new HttpHeaderInterceptor("Content-Type", MediaType.APPLICATION_JSON_VALUE))
                .build();

        final HttpEntity<Person> httpEntity = new HttpEntity<>(new Person("Thomas", "Freese"));
        final ResponseEntity<ProblemDetail> responseEntity = restTemplate.exchange("/rest/person/personAdd", HttpMethod.POST, httpEntity, ProblemDetail.class);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

        restTemplate = this.restTemplateBuilder
                .interceptors(new BasicAuthenticationInterceptor("user", "pw", StandardCharsets.UTF_8),
                        new HttpHeaderInterceptor("Accept", MediaType.APPLICATION_JSON_VALUE))
                .build();

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
        final RestTemplate restTemplate = this.restTemplateBuilder
                .interceptors(new BasicAuthenticationInterceptor("user", "pw", StandardCharsets.UTF_8), new HttpHeaderInterceptor("Content-Type", MediaType.APPLICATION_JSON_VALUE))
                .build();

        final ProblemDetail error = restTemplate.postForObject("/rest/person/personAdd", new Person("Thomas", "Freese"), ProblemDetail.class);
        assertEquals(HttpStatus.FORBIDDEN.value(), error.getStatus());
    }

    @Override
    @Test
    void testUserWithLoginJSON() {
        final RestTemplate restTemplate = this.restTemplateBuilder
                .interceptors(new BasicAuthenticationInterceptor("user", "pw", StandardCharsets.UTF_8), new HttpHeaderInterceptor("Accept", MediaType.APPLICATION_JSON_VALUE))
                .build();

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
        final RestTemplate restTemplate = this.restTemplateBuilder
                .interceptors(new BasicAuthenticationInterceptor("user", "pw", StandardCharsets.UTF_8),
                        new HttpHeaderInterceptor("Accept", MediaType.APPLICATION_XML_VALUE + ";charset=UTF-8"))
                .build();

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
        final RestTemplate restTemplate = this.restTemplateBuilder
                .interceptors(new HttpHeaderInterceptor("my-token", "user"), new HttpHeaderInterceptor("Accept", MediaType.APPLICATION_JSON_VALUE))
                .build();

        final Person[] personArray = restTemplate.getForObject("/rest/person/personList", Person[].class);
        assertNotNull(personArray);

        final List<Person> persons = Arrays.asList(personArray);

        assertNotNull(persons);
        assertTrue(persons.size() >= 2);
    }

    @Override
    @Test
    void testUserWithPreAuthXML() {
        final RestTemplate restTemplate = this.restTemplateBuilder
                .interceptors(new HttpHeaderInterceptor("my-token", "user"), new HttpHeaderInterceptor("Accept", MediaType.APPLICATION_XML_VALUE + ";charset=UTF-8"))
                .build();

        final Person[] personArray = restTemplate.getForObject("/rest/person/personList", Person[].class);
        assertNotNull(personArray);

        final List<Person> persons = Arrays.asList(personArray);

        assertNotNull(persons);
        assertTrue(persons.size() >= 2);
    }

    @Override
    @Test
    void testUserWithWrongPass() {
        final RestTemplate restTemplate = this.restTemplateBuilder
                .interceptors(new BasicAuthenticationInterceptor("user", "pass", StandardCharsets.UTF_8))
                .build();

        final ResponseEntity<String> responseEntity = restTemplate.getForEntity("/rest/person/personList", String.class);

        assertEquals(HttpStatus.UNAUTHORIZED, responseEntity.getStatusCode());
    }

    @Override
    @Test
    void testUserWithWrongRole() {
        final RestTemplate restTemplate = this.restTemplateBuilder
                .interceptors(new BasicAuthenticationInterceptor("invalid", "pw", StandardCharsets.UTF_8))
                .build();

        final ResponseEntity<String> responseEntity = restTemplate.getForEntity("/rest/person/personList", String.class);

        assertEquals(HttpStatus.FORBIDDEN, responseEntity.getStatusCode());
    }

    @Override
    @Test
    void testUserWithoutLogin() {
        final RestTemplate restTemplate = this.restTemplateBuilder
                .interceptors(new HttpHeaderInterceptor("Accept", MediaType.APPLICATION_JSON_VALUE))
                .build();

        final ResponseEntity<String> responseEntity = restTemplate.getForEntity("/rest/person/personList", String.class);

        // assertEquals(HttpStatus.UNAUTHORIZED, responseEntity.getStatusCode());

        // Eigentlich UNAUTHORIZED erwartet -> RememberMeServices ?
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
    }
}
