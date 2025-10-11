// Created: 11.10.2025
package de.freese.spring.data.jpa.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import jakarta.annotation.Resource;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;

import de.freese.spring.data.jpa.domain.Todo;

/**
 * @author Thomas Freese
 */
class TestWebTestClient extends AbstractClientTest {
    @Resource
    private WebTestClient webTestClient;

    @BeforeEach
    void beforeEach() {
        // webTestClient = WebTestClient.bindToController(new TodoController(new TodoService(repository))).build();
        // webTestClient = WebTestClient
        //         .bindToServer()
        //         .baseUrl("http://localhost:" + localServerPort)
        //         .codecs(configurer -> {
        //             // configurer.registerDefaults(true);
        //             configurer.defaultCodecs().jaxb2Encoder(new Jaxb2XmlEncoder());
        //             configurer.defaultCodecs().jaxb2Decoder(new Jaxb2XmlDecoder());
        //             // configurer.defaultCodecs().jackson2JsonEncoder(new Jackson2JsonEncoder());
        //             // configurer.defaultCodecs().jackson2JsonDecoder(new Jackson2JsonDecoder());
        //         }).build();
    }

    @Test
    @Override
    void testCreateTodo() {
        final Todo todo = creaTodo();

        webTestClient.post()
                .uri("/api/todo")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(todo))
                .exchange()
                .expectStatus().isCreated()
                .expectBody(Todo.class).value(value -> {
                    getLogger().info(String.valueOf(value));
                    assertNotNull(value);
                });
    }

    @Test
    @Override
    void testGetAllTodosJson() {
        testCreateTodo();

        webTestClient.get()
                .uri("/api/todo")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class).value(value -> {
                    getLogger().info(value);
                    assertNotNull(value);
                    assertTrue(value.startsWith("[ {"));
                    assertTrue(value.strip().endsWith("} ]"));
                })
        ;

        webTestClient.get()
                .uri("/api/todo")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Todo.class).value(list -> {
                    assertNotNull(list);
                    assertFalse(list.isEmpty());
                    getLogger().info(list.toString());
                })
        ;
    }

    @Test
    @Override
    void testGetAllTodosXml() {
        testCreateTodo();

        webTestClient.get()
                .uri("/api/todo")
                .accept(MediaType.APPLICATION_XML)
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class).value(value -> {
                    getLogger().info(value);
                    assertNotNull(value);
                    assertTrue(value.startsWith("<"));
                    assertTrue(value.strip().endsWith(">"));
                })
        ;

        webTestClient.get()
                .uri("/api/todo")
                .accept(MediaType.APPLICATION_XML)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Todo.class).value(list -> {
                    getLogger().info(list.toString());
                    assertNotNull(list);
                    // assertFalse(list.isEmpty());
                })
        ;
    }

    @Test
    @Override
    void testNotFound() {
        webTestClient.get()
                .uri("/api/todo/" + UUID.randomUUID())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound()
                // .expectBody(ProblemDetail.class).value(System.out::println)
                .expectBody(String.class).value(value -> {
                    getLogger().info(value);
                    assertNotNull(value);
                    assertTrue(value.contains("Todo not found by ID:"));
                })
        ;
    }

    @Test
    @Override
    void testStream() {
        webTestClient.post()
                .uri("/api/todo/" + UUID.randomUUID() + "/stream")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(BodyInserters.fromResource(new InputStreamResource(new ByteArrayInputStream("From Client: Hello World".getBytes(StandardCharsets.UTF_8)))))
                // .bodyValue("From Client: Hello World".getBytes(StandardCharsets.UTF_8)) // Alternative
                .exchange()
                .expectStatus()
                .isOk()
        ;

        // restClient.get().uri("...").retrieve().body(InputStreamResource.class);
        webTestClient.get()
                .uri("/api/todo/" + UUID.randomUUID() + "/stream")
                .accept(MediaType.APPLICATION_OCTET_STREAM)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(InputStreamResource.class)
                .value(inputStreamResource -> {
                    // inputStreamResource.getContentAsString(StandardCharsets.UTF_8);
                    // Reader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
                    // BufferedReader bufferedReader = new BufferedReader(reader)
                    try (InputStream inputStream = inputStreamResource.getInputStream()) {
                        // final String message = bufferedReader.lines().collect(Collectors.joining(System.lineSeparator()));
                        final String message = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
                        assertEquals("From Server: Hello World", message);
                        getLogger().info(message);
                    }
                    catch (IOException ex) {
                        throw new UncheckedIOException(ex);
                    }
                })
        ;
    }
}
