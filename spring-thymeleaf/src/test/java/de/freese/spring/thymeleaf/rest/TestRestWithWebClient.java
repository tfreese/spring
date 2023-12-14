// Created: 07.09.2018
package de.freese.spring.thymeleaf.rest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.util.List;

import jakarta.annotation.Resource;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.reactive.function.client.ExchangeFilterFunctions;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import de.freese.spring.thymeleaf.ThymeleafApplication;
import de.freese.spring.thymeleaf.model.Person;

/**
 * @author Thomas Freese
 */
@ActiveProfiles("test")
class TestRestWithWebClient extends AbstractRestTestCase {
    @Resource
    private WebClient.Builder webClientBuilder;
    // /**
    // * Spring-Boot will create a `WebTestClient` for you<br>
    // * already configure and ready to issue requests against "localhost:RANDOM_PORT"
    // */
    // @Resource
    // private WebTestClient webTestClient = null;

    @BeforeEach
    void beforeTest() throws Exception {
        final String rootUri = ThymeleafApplication.getRootUri(getEnvironment());

        // @formatter:off
//        ExchangeStrategies strategies = ExchangeStrategies.builder()
//                .codecs(configurer -> {
//                    configurer.defaultCodecs().jackson2JsonEncoder(new Jackson2JsonEncoder(this.objectMapper, MediaType.APPLICATION_JSON));
//                    configurer.defaultCodecs().jackson2JsonDecoder(new Jackson2JsonDecoder(this.objectMapper, MediaType.APPLICATION_JSON));
//
//                }).build();

        this.webClientBuilder.baseUrl(rootUri)
            //.exchangeStrategies(strategies)
            ;
        // @formatter:on
    }

    @Override
    @Test
    void testHealthEndpoint() throws Exception {
        // @formatter:off
        final WebClient webClient = this.webClientBuilder.build();

//        final RequestHeadersSpec<?> request = webClient.get()
//                .repository("/actuator/health")
//                .accept(MediaType.APPLICATION_JSON_UTF8)
//                ;

//        final Mono<String> response = webClient.get()
//                .repository("/actuator/health")
//                .accept(MediaType.APPLICATION_JSON_UTF8)
//                .retrieve() // Liefert nur den Body.
//                .bodyToMono(String.class)
//                ;

//        final Mono<String> response = webClient.get()
//                .repository("/actuator/health")
//                .accept(MediaType.APPLICATION_JSON_UTF8)
//                .exchange() // Liefert auch Header und Status.
//                .doOnSuccess(clientResponse -> Assertions.assertEquals(MediaType.APPLICATION_JSON_VALUE, clientResponse.headers().asHttpHeaders().getFirst("Content-Type")))
//                .flatMap(clientResponse -> clientResponse.bodyToMono(String.class))
//                ;

        final Mono<ResponseEntity<String>> response = webClient.get()
                .uri("/actuator/health")
                .accept(MediaType.APPLICATION_JSON)
                .exchangeToMono(clientResponse -> clientResponse.toEntity(String.class)) // Liefert Header, Status und ResponseBody.
                ;
        // @formatter:on

        final ResponseEntity<String> responseEntity = response.block();

        assertEquals(MediaType.APPLICATION_JSON, responseEntity.getHeaders().getContentType());

        final String status = JsonPath.parse(responseEntity.getBody()).read("$.status");
        assertEquals("UP", status);
    }

    @Override
    @Test
    void testPost() throws Exception {
        final WebClient webClient = this.webClientBuilder.build();
        final Person newPerson = new Person("Thomas", "Freese");

        // @formatter:off
        final Mono<ResponseEntity<String>> response = webClient.mutate()
                .filter(ExchangeFilterFunctions.basicAuthentication("admin", "pw"))
                .build()
                .post()
                .uri("/rest/person/personAdd")
                .contentType(MediaType.APPLICATION_JSON)
                //.body(Mono.just(newPerson), Person.class)
                // ist das gleiche wie '.body(BodyInserters.fromObject(newPerson))'
                .bodyValue(newPerson)
                .exchangeToMono(clientResponse -> clientResponse.toEntity(String.class))
                ;
        // @formatter:on

        // ResponseEntity<ApiError> responseEntity = response.block();
        final ResponseEntity<String> responseEntity = response.block();
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

        // @formatter:off
        final Flux<Person> personFlux = webClient.mutate()
                   .filter(ExchangeFilterFunctions.basicAuthentication("user", "pw"))
                   .build()
                   .get()
                   .uri("/rest/person/personList")
                   .accept(MediaType.APPLICATION_JSON)
                   .retrieve()
                   .bodyToFlux(Person.class)
                   ;
        // @formatter:on

        final List<Person> persons = personFlux.collectList().block();

        assertNotNull(persons);
        assertTrue(persons.size() >= 3);

        assertEquals("Thomas", persons.get(persons.size() - 1).getFirstName());
        assertEquals("Freese", persons.get(persons.size() - 1).getLastName());
    }

    @Override
    @Test
    void testPostWithWrongRole() throws Exception {
        final WebClient webClient = this.webClientBuilder.build();
        final Person newPerson = new Person("Thomas", "Freese");

        // @formatter:off
        final Mono<ResponseEntity<String>> response = webClient.mutate()
                .filter(ExchangeFilterFunctions.basicAuthentication("user", "pw"))
                .build()
                .post()
                .uri("/rest/person/personList")
                .contentType(MediaType.APPLICATION_JSON)
                //.body(Mono.just(newPerson), Person.class)
                // ist das gleiche wie '.body(BodyInserters.fromObject(newPerson))'
                .bodyValue(newPerson)
                .exchangeToMono(clientResponse -> clientResponse.toEntity(String.class))
                ;
        // @formatter:on

        // final ResponseEntity<ApiError> responseEntity = response.block();
        final ResponseEntity<String> responseEntity = response.block();
        // final ApiError apiError = responseEntity.getBody();

        //        assertEquals(HttpStatus.FORBIDDEN.value(), responseEntity.getStatusCode());
        //        assertEquals(HttpStatus.METHOD_NOT_ALLOWED, responseEntity.getStatusCode());
        assertEquals(HttpStatus.UNAUTHORIZED, responseEntity.getStatusCode());
    }

    @Override
    @Test
    void testUserWithLoginJSON() throws Exception {
        final WebClient webClient = this.webClientBuilder.build();

        // @formatter:off
        final Flux<Person> personFlux = webClient.mutate()
                   .filter(ExchangeFilterFunctions.basicAuthentication("user", "pw"))
                   .build()
                   .get()
                   .uri("/rest/person/personList")
                   .accept(MediaType.APPLICATION_JSON)
                   .retrieve()
                   .bodyToFlux(Person.class)
                   ;
        // @formatter:on

        final List<Person> persons = personFlux.collectList().block();

        assertNotNull(persons);
        assertTrue(persons.size() >= 2);
    }

    @Override
        // @Test
    void testUserWithLoginXML() throws Exception {
        final WebClient webClient = this.webClientBuilder.build();

        // @formatter:off
        final Flux<Person> personFlux = webClient.mutate()
                   .filter(ExchangeFilterFunctions.basicAuthentication("user", "pw"))
                   .build()
                   .get()
                   .uri("/rest/person/personList")
                   .accept(MediaType.APPLICATION_XML)
                   .acceptCharset(StandardCharsets.UTF_8)
                   //.header("Accept", MediaType.APPLICATION_XML_VALUE+";charset=UTF-8")
                   .retrieve()
                   .bodyToFlux(Person.class)
                   ;
        // @formatter:on

        final List<Person> persons = personFlux.collectList().block();

        assertNotNull(persons);
        assertTrue(persons.size() >= 2);
    }

    @Override
    @Test
    void testUserWithPreAuthJSON() throws Exception {
        final WebClient webClient = this.webClientBuilder.build();

        // @formatter:off
        final Flux<Person> personFlux = webClient
                   .get()
                   .uri("/rest/person/personList")
                   .accept(MediaType.APPLICATION_JSON)
                   .acceptCharset(StandardCharsets.UTF_8)
                   .header("my-token", "user")
                   .retrieve()
                   .bodyToFlux(Person.class)
                   ;
        // @formatter:on

        final List<Person> persons = personFlux.collectList().block();

        assertNotNull(persons);
        assertTrue(persons.size() >= 2);
    }

    @Override
        // @Test
    void testUserWithPreAuthXML() throws Exception {
        final WebClient webClient = this.webClientBuilder.build();

        // @formatter:off
        final Flux<Person> personFlux = webClient
                   .get()
                   .uri("/rest/person/personList")
                   .accept(MediaType.APPLICATION_XML)
                   .acceptCharset(StandardCharsets.UTF_8)
                   .header("my-token", "user")
                   .retrieve()
                   .bodyToFlux(Person.class)
                   ;
        // @formatter:on

        final List<Person> persons = personFlux.collectList().block();

        assertNotNull(persons);
        assertTrue(persons.size() >= 2);
    }

    @Override
    @Test
    void testUserWithWrongPass() throws Exception {
        final WebClient webClient = this.webClientBuilder.build();

        // @formatter:off
//        final Flux<Person> personFlux = webClient.mutate()
//                .filter(ExchangeFilterFunctions.basicAuthentication("user", "pass"))
//                .build()
//                .get()
//                .repository("/rest/person/personList")
//                .accept(MediaType.APPLICATION_JSON_UTF8)
//                .retrieve()
//                .onStatus(status ->  !HttpStatus.UNAUTHORIZED.equals(status), response -> Mono.just(new Exception()))
//                .bodyToFlux(Person.class)
//                ;
        final Mono<ResponseEntity<String>> response = webClient.mutate()
                .filter(ExchangeFilterFunctions.basicAuthentication("user", "pass"))
                .build()
                .get()
                .uri("/rest/person/personList")
                .accept(MediaType.APPLICATION_JSON)
                .exchangeToMono(clientResponse -> clientResponse.toEntity(String.class))
                ;
        // @formatter:on

        final ResponseEntity<String> responseEntity = response.block();

        assertEquals(HttpStatus.UNAUTHORIZED, responseEntity.getStatusCode());
    }

    @Override
    @Test
    void testUserWithWrongRole() throws Exception {
        final WebClient webClient = this.webClientBuilder.build();

        // @formatter:off
        final Mono<ResponseEntity<String>> response = webClient.mutate()
                .filter(ExchangeFilterFunctions.basicAuthentication("invalid", "pw"))
                .build()
                .get()
                .uri("/rest/person/personList")
                .accept(MediaType.APPLICATION_JSON)
                .exchangeToMono(clientResponse -> clientResponse.toEntity(String.class))
                ;
        // @formatter:on

        final ResponseEntity<String> responseEntity = response.block();

        assertEquals(HttpStatus.FORBIDDEN, responseEntity.getStatusCode());
    }

    @Override
    @Test
    void testUserWithoutLogin() throws Exception {
        final WebClient webClient = this.webClientBuilder.build();

        // @formatter:off
//        final Flux<Person> personFlux = webClient.get()
//                .repository("/rest/person/personList")
//                .accept(MediaType.APPLICATION_JSON_UTF8)
//                .retrieve()
//                .onStatus(status ->  !HttpStatus.UNAUTHORIZED.equals(status) , response -> Mono.just(new Exception()))
//                .bodyToFlux(Person.class)

//                .exchange()
//                .flatMapIterable(clientResponse -> clientResponse.bodyToFlux(Person.class))
//                ;
        final Mono<ResponseEntity<String>> response = webClient.get()
                .uri("/rest/person/personList")
                .accept(MediaType.APPLICATION_JSON)
                .exchangeToMono(clientResponse -> clientResponse.toEntity(String.class))
                ;
        // @formatter:on

        final ResponseEntity<String> responseEntity = response.block();

        assertEquals(HttpStatus.UNAUTHORIZED, responseEntity.getStatusCode());
    }
}
