// Created: 16.08.23
package de.freese.spring.data.jpa;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.stream.Collectors;

import jakarta.annotation.Resource;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;

/**
 * @author Thomas Freese
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
class TodoApplicationTests {

    @Resource
    private WebTestClient webTestClient;

    @Test
    void contextLoads() {
        assertTrue(true);
    }

    @Test
    void testStreams() {
        // @formatter:off
        webTestClient.post()
                .uri("/api/todo/" + UUID.randomUUID() + "/stream")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(BodyInserters.fromResource(new InputStreamResource(new ByteArrayInputStream("From Client: Hello World".getBytes(StandardCharsets.UTF_8)))))
                // .bodyValue("From Client: Hello World".getBytes(StandardCharsets.UTF_8)) // Alternative
                .exchange()
                .expectStatus()
                .isOk()
        ;
        // @formatter:on

        // @formatter:off
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
        // @formatter:on
    }
}
