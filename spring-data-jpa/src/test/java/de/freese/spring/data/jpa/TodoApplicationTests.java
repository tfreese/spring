// Created: 16.08.23
package de.freese.spring.data.jpa;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.stream.Collectors;

import jakarta.annotation.Resource;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import de.freese.spring.data.jpa.domain.Status;
import de.freese.spring.data.jpa.domain.Todo;
import de.freese.spring.data.jpa.exception.ApplicationException;

/**
 * <pre>{@code
 * @ExtendWith(SpringExtension.class)
 * @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
 * }</pre>
 *
 * @author Thomas Freese
 */
@SpringBootTest(properties = "server.port=0", webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class TodoApplicationTests {

    @LocalServerPort
    private int localServerPort;
    @Resource
    private RestClient.Builder restClientBuilder;
    @Resource
    private WebTestClient webTestClient;

    @BeforeEach
    void beforeEach() {
        // webTestClient = WebTestClient.bindToController(new TodoController(new TodoService(repository))).build();
        // webTestClient = WebTestClient
        //         .bindToServer()
        //         .baseUrl("http://localhost:" + localServerPort)
        //         .codecs(configurer -> {
        //             //                  configurer.registerDefaults(true);
        //             configurer.defaultCodecs().jaxb2Encoder(new Jaxb2XmlEncoder());
        //             configurer.defaultCodecs().jaxb2Decoder(new Jaxb2XmlDecoder());
        //             //                   configurer.defaultCodecs().jackson2JsonEncoder(new Jackson2JsonEncoder());
        //             //                  configurer.defaultCodecs().jackson2JsonDecoder(new Jackson2JsonDecoder());
        //         }).build();
    }

    @Test
    void testGetAllTodosJson() {
        testCreateTodo();

        webTestClient.get()
                .uri("/api/todo")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class).value(value -> {
                    System.out.println(value);
                    assertNotNull(value);
                })
        ;

        webTestClient.get()
                .uri("/api/todo")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Todo.class).value(list -> {
                    System.out.println(list);
                    assertNotNull(list);
                    assertFalse(list.isEmpty());
                })
        ;

        // final Function<ClientResponse, Mono<ClientResponse>> exceptionFilterFunction = clientResponse -> {
        //     final HttpStatus statusCode = HttpStatus.resolve(clientResponse.statusCode().value());
        //
        //     if (!HttpStatus.OK.equals(statusCode)) {
        //         return clientResponse.bodyToMono(String.class).flatMap(body -> Mono.error(new ApplicationException(body)));
        //     }
        //
        //     return Mono.just(clientResponse);
        // };

        final WebClient webClient = WebClient.builder()
                .baseUrl("http://localhost:" + localServerPort)
                // .filter(ExchangeFilterFunction.ofResponseProcessor(exceptionFilterFunction))
                .build();

        webClient.get()
                .uri("/api/todo")
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                // .onStatus(status -> status != HttpStatus.OK, clientResponse -> Mono.empty())
                .onStatus(status -> status != HttpStatus.OK, clientResponse -> clientResponse.bodyToMono(String.class).map(ApplicationException::new))
                .bodyToFlux(Todo.class)
                //.exchangeToFlux(response -> response.bodyToFlux(Todo.class))
                //                .blockOptional().ifPresent(System.out::println)
                .doOnNext(value -> {
                    System.out.println(value);
                    assertNotNull(value);
                }).blockLast();
    }

    @Test
    void testGetAllTodosXml() {
        testCreateTodo();

        webTestClient.get()
                .uri("/api/todo")
                .accept(MediaType.APPLICATION_XML)
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class).value(value -> {
                    System.out.println(value);
                    assertNotNull(value);
                })
        ;

        webTestClient.get()
                .uri("/api/todo")
                .accept(MediaType.APPLICATION_XML)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Todo.class).value(list -> {
                    System.out.println(list);
                    assertNotNull(list);
                    // assertFalse(list.isEmpty());
                })
        ;
    }

    @Test
    void testNotFoundRestClient() {
        final RestClient restClient = restClientBuilder.baseUrl("http://localhost:" + localServerPort).build();

        final HttpClientErrorException exception = assertThrows(HttpClientErrorException.NotFound.class,
                () -> restClient.get().uri("/api/todo/" + UUID.randomUUID()).accept(MediaType.APPLICATION_JSON).retrieve()
                        //                        .onStatus(new DefaultResponseErrorHandler() {
                        //                            @Override
                        //                            public boolean hasError(final ClientHttpResponse response) throws IOException {
                        //                                return false;
                        //                            }
                        //                        })
                        .body(Todo.class));

        assertEquals(HttpStatus.NOT_FOUND.value(), exception.getStatusCode().value());

        // final ProblemDetail problemDetail = exception.getResponseBodyAs(ProblemDetail.class);
        final String problemDetail = exception.getResponseBodyAs(String.class);
        System.out.println(problemDetail);
        assertNotNull(problemDetail);
        assertTrue(problemDetail.contains("Todo not found by ID:"));
    }

    @Test
    void testNotFoundWebTestClient() {
        webTestClient.get()
                .uri("/api/todo/" + UUID.randomUUID())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound()
                // .expectBody(ProblemDetail.class).value(System.out::println)
                .expectBody(String.class).value(value -> {
                    System.out.println(value);
                    assertNotNull(value);
                    assertTrue(value.contains("Todo not found by ID:"));
                })
        ;
    }

    @Test
    void testStreams() {
        webTestClient.post()
                .uri("/api/todo/" + UUID.randomUUID() + "/stream")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(BodyInserters.fromResource(new InputStreamResource(new ByteArrayInputStream("From Client: Hello World".getBytes(StandardCharsets.UTF_8)))))
                // .bodyValue("From Client: Hello World".getBytes(StandardCharsets.UTF_8)) // Alternative
                .exchange()
                .expectStatus()
                .isOk()
        ;

        webTestClient.get()
                .uri("/api/todo/" + UUID.randomUUID() + "/stream")
                .accept(MediaType.APPLICATION_OCTET_STREAM).exchange()
                .expectStatus()
                .isOk()
                .expectBody(InputStreamResource.class)
                .value(inputStreamResource -> {
                    try (InputStream inputStream = inputStreamResource.getInputStream();
                         Reader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
                         BufferedReader bufferedReader = new BufferedReader(reader)) {
                        final String message = bufferedReader.lines().collect(Collectors.joining(System.lineSeparator()));
                        assertEquals("From Server: Hello World", message);
                    }
                    catch (IOException ex) {
                        throw new UncheckedIOException(ex);
                    }
                })
        ;
    }

    private void testCreateTodo() {
        final Todo todo = new Todo();
        todo.setName("Test");
        todo.setStartTime(LocalDateTime.now());
        todo.setEndTime(LocalDateTime.now().plusDays(1));
        todo.setTaskStatus(Status.PENDING);

        webTestClient.post()
                .uri("/api/todo")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(todo)).exchange()
                .expectStatus().isCreated()
                .expectBody(Todo.class).value(value -> {
                    System.out.println(value);
                    assertNotNull(value);
                });
    }
}
