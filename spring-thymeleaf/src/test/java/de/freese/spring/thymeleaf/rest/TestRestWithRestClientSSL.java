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
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.support.BasicAuthenticationInterceptor;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestClient;

import de.freese.spring.thymeleaf.HttpHeaderInterceptor;
import de.freese.spring.thymeleaf.ThymeleafApplication;
import de.freese.spring.thymeleaf.model.Person;

/**
 * @author Thomas Freese
 * @since 07.09.2018
 */
@ActiveProfiles({"test", "with-ssl"})
class TestRestWithRestClientSSL extends AbstractRestTestCase {
    @Resource
    private RestClient.Builder restClientBuilder;

    @BeforeEach
    void beforeTest() {
        // final String rootUri = "http://localhost:" + localServerPort;
        final String rootUri = ThymeleafApplication.getRootUri(getEnvironment());

        restClientBuilder = restClientBuilder
                .baseUrl(rootUri)
                .defaultStatusHandler(new NoOpResponseErrorHandler());
    }

    @Override
    @Test
    void testHealthEndpoint() {
        final RestClient restClient = restClientBuilder
                .requestInterceptor(new HttpHeaderInterceptor("Accept", MediaType.APPLICATION_JSON_VALUE))
                .build();

        final ResponseEntity<String> responseEntity = restClient.get()
                .uri("/actuator/info")
                .retrieve()
                .toEntity(String.class);

        assertEquals(MediaType.APPLICATION_JSON, responseEntity.getHeaders().getContentType());
        assertEquals(HttpStatus.OK.value(), responseEntity.getStatusCode().value());

        // final String status = JsonPath.parse(responseEntity.getBody()).read("$.status");
        // assertEquals("UP", status);
    }

    @Override
    @Test
    void testPost() {
        RestClient restClient = restClientBuilder
                .requestInterceptors(consumer -> {
                    consumer.add(new BasicAuthenticationInterceptor("admin", "pw", StandardCharsets.UTF_8));
                    consumer.add(new HttpHeaderInterceptor("Content-Type", MediaType.APPLICATION_JSON_VALUE));
                })
                .build();

        // final HttpEntity<Person> httpEntity = new HttpEntity<>(new Person("Thomas", "Freese"));
        final ResponseEntity<ProblemDetail> responseEntity = restClient.post()
                .uri("/rest/person/personAdd")
                .body(new Person("Thomas", "Freese"))
                .retrieve()
                .toEntity(ProblemDetail.class);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

        restClient = restClientBuilder
                .requestInterceptors(consumer -> {
                    consumer.add(new BasicAuthenticationInterceptor("user", "pw", StandardCharsets.UTF_8));
                    consumer.add(new HttpHeaderInterceptor("Accept", MediaType.APPLICATION_JSON_VALUE));
                })
                .build();

        final Person[] personArray = restClient.get()
                .uri("/rest/person/personList")
                .retrieve()
                .toEntity(Person[].class)
                .getBody();
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
        final RestClient restClient = restClientBuilder
                .requestInterceptors(consumer -> {
                    consumer.add(new BasicAuthenticationInterceptor("user", "pw", StandardCharsets.UTF_8));
                    consumer.add(new HttpHeaderInterceptor("Accept", MediaType.APPLICATION_JSON_VALUE));
                })
                .build();

        final ProblemDetail error = restClient.post()
                .uri("/rest/person/personAdd")
                .body(new Person("Thomas", "Freese"))
                .retrieve()
                .toEntity(ProblemDetail.class)
                .getBody();
        assertNotNull(error);
        assertEquals(HttpStatus.FORBIDDEN.value(), error.getStatus());
    }

    @Override
    @Test
    void testUserWithLoginJSON() {
        final RestClient restClient = restClientBuilder
                .requestInterceptors(consumer -> {
                    consumer.add(new BasicAuthenticationInterceptor("user", "pw", StandardCharsets.UTF_8));
                    consumer.add(new HttpHeaderInterceptor("Accept", MediaType.APPLICATION_JSON_VALUE));
                })
                .build();

        // final ResponseEntity<String> responseEntity = restTemplate.getForEntity("/rest/person/personList", String.class);
        // persons = objectMapper.readValue(responseEntity.getBody(), new TypeReference<List<Person>>()
        // {
        // });

        // final Person[] personArray = restTemplate.getForObject("/rest/person/personList", Person[].class);
        // persons = Arrays.asList(personArray);

        final ResponseEntity<List<Person>> responseEntity = restClient.get()
                .uri("/rest/person/personList")
                .retrieve()
                .toEntity(new ParameterizedTypeReference<>() {
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
        final RestClient restClient = restClientBuilder
                .requestInterceptors(consumer -> {
                    consumer.add(new BasicAuthenticationInterceptor("user", "pw", StandardCharsets.UTF_8));
                    consumer.add(new HttpHeaderInterceptor("Accept", MediaType.APPLICATION_XML_VALUE + ";charset=UTF-8"));
                })
                .build();

        // // ResponseEntity<String> responseEntity = restTemplate.getForEntity("/rest/person/personList", String.class);
        // persons = objectMapper.readValue(responseEntity.getBody(), new TypeReference<List<Person>>()
        // {
        // });

        // final Person[] personArray = restTemplate.getForObject("/rest/person/personList", Person[].class);
        // persons = Arrays.asList(personArray);

        final ResponseEntity<List<Person>> responseEntity = restClient.get()
                .uri("/rest/person/personList")
                .retrieve()
                .toEntity(new ParameterizedTypeReference<>() {
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
        final RestClient restClient = restClientBuilder
                .requestInterceptors(consumer -> {
                    consumer.add(new HttpHeaderInterceptor("my-token", "user"));
                    consumer.add(new HttpHeaderInterceptor("Accept", MediaType.APPLICATION_JSON_VALUE));
                })
                .build();

        final Person[] personArray = restClient.get()
                .uri("/rest/person/personList")
                .retrieve()
                .toEntity(Person[].class)
                .getBody();
        assertNotNull(personArray);

        final List<Person> persons = Arrays.asList(personArray);
        assertNotNull(persons);
        assertTrue(persons.size() >= 2);
    }

    @Override
    @Test
    void testUserWithPreAuthXML() {
        final RestClient restClient = restClientBuilder
                .requestInterceptors(consumer -> {
                    consumer.add(new HttpHeaderInterceptor("my-token", "user"));
                    consumer.add(new HttpHeaderInterceptor("Accept", MediaType.APPLICATION_XML_VALUE + ";charset=UTF-8"));
                })
                .build();

        final Person[] personArray = restClient.get()
                .uri("/rest/person/personList")
                .retrieve()
                .toEntity(Person[].class)
                .getBody();
        assertNotNull(personArray);

        final List<Person> persons = Arrays.asList(personArray);
        assertNotNull(persons);
        assertTrue(persons.size() >= 2);
    }

    @Override
    @Test
    void testUserWithWrongPass() {
        final RestClient restClient = restClientBuilder
                .requestInterceptor(new BasicAuthenticationInterceptor("user", "pass", StandardCharsets.UTF_8))
                .build();

        final ResponseEntity<String> responseEntity = restClient.get()
                .uri("/rest/person/personList")
                .retrieve()
                .toEntity(String.class);
        assertEquals(HttpStatus.UNAUTHORIZED, responseEntity.getStatusCode());
    }

    @Override
    @Test
    void testUserWithWrongRole() {
        final RestClient restClient = restClientBuilder
                .requestInterceptor(new BasicAuthenticationInterceptor("invalid", "pw", StandardCharsets.UTF_8))
                .build();

        final ResponseEntity<String> responseEntity = restClient.get()
                .uri("/rest/person/personList")
                .retrieve()
                .toEntity(String.class);
        assertEquals(HttpStatus.FORBIDDEN, responseEntity.getStatusCode());
    }

    @Override
    @Test
    void testUserWithoutLogin() {
        final RestClient restClient = restClientBuilder
                .requestInterceptor(new HttpHeaderInterceptor("Accept", MediaType.APPLICATION_JSON_VALUE))
                .build();

        final ResponseEntity<String> responseEntity = restClient.get()
                .uri("/rest/person/personList")
                .retrieve()
                .toEntity(String.class);
        // assertEquals(HttpStatus.UNAUTHORIZED, responseEntity.getStatusCode());

        assertEquals(HttpStatus.UNAUTHORIZED, responseEntity.getStatusCode());
    }
}
