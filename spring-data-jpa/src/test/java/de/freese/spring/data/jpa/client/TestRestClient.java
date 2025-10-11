// Created: 11.10.2025
package de.freese.spring.data.jpa.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

import jakarta.annotation.Resource;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import de.freese.spring.data.jpa.domain.Todo;

/**
 * @author Thomas Freese
 */
class TestRestClient extends AbstractClientTest {

    private RestClient restClient;

    @Resource
    private RestClient.Builder restClientBuilder;

    @BeforeEach
    void beforeEach() {
        restClient = restClientBuilder.baseUrl(getUrl()).build();
    }

    @Test
    @Override
    void testCreateTodo() {
        final Todo todo = creaTodo();

        final ResponseEntity<String> responseEntity = restClient.post()
                .uri("/api/todo")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .body(todo)
                .retrieve()
                .toEntity(String.class);

        getLogger().info(responseEntity.getBody());

        assertEquals(HttpStatus.CREATED.value(), responseEntity.getStatusCode().value());
        assertNotNull(responseEntity.getBody());
    }

    @Test
    @Override
    void testGetAllTodosJson() {
        testCreateTodo();

        final ResponseEntity<String> responseEntityString = restClient.get()
                .uri("/api/todo")
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .toEntity(String.class);

        getLogger().info(responseEntityString.getBody());
        assertEquals(HttpStatus.OK.value(), responseEntityString.getStatusCode().value());
        assertNotNull(responseEntityString.getBody());
        assertTrue(responseEntityString.getBody().startsWith("[ {"));
        assertTrue(responseEntityString.getBody().strip().endsWith("} ]"));

        final ResponseEntity<List<Todo>> responseEntityList = restClient.get()
                .uri("/api/todo")
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .toEntity(new ParameterizedTypeReference<>() {
                });

        assertNotNull(responseEntityList.getBody());
        assertFalse(responseEntityList.getBody().isEmpty());
        getLogger().info(responseEntityList.getBody().toString());
    }

    @Test
    @Override
    void testGetAllTodosXml() {
        testCreateTodo();

        final ResponseEntity<String> responseEntityString = restClient.get()
                .uri("/api/todo")
                .accept(MediaType.APPLICATION_XML)
                .retrieve()
                .toEntity(String.class);
        getLogger().info(responseEntityString.getBody());
        assertEquals(HttpStatus.OK.value(), responseEntityString.getStatusCode().value());
        assertNotNull(responseEntityString.getBody());
        assertTrue(responseEntityString.getBody().startsWith("<"));
        assertTrue(responseEntityString.getBody().strip().endsWith(">"));

        final ResponseEntity<List<Todo>> responseEntityList = restClient.get()
                .uri("/api/todo")
                .accept(MediaType.APPLICATION_XML)
                .retrieve()
                .toEntity(new ParameterizedTypeReference<>() {
                });
        assertNotNull(responseEntityList.getBody());
        assertFalse(responseEntityList.getBody().isEmpty());
        getLogger().info(responseEntityList.getBody().toString());
    }

    @Test
    @Override
    void testNotFound() {
        final HttpClientErrorException exception = assertThrows(HttpClientErrorException.NotFound.class, () -> restClient.get()
                .uri("/api/todo/" + UUID.randomUUID())
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .toEntity(String.class)
        );

        assertNotNull(exception);
        assertEquals(HttpStatus.NOT_FOUND.value(), exception.getStatusCode().value());

        // final ProblemDetail problemDetail = exception.getResponseBodyAs(ProblemDetail.class);
        final String problemDetail = exception.getResponseBodyAs(String.class);
        getLogger().info(problemDetail);
        assertNotNull(problemDetail);
        assertTrue(problemDetail.contains("Todo not found by ID:"));
    }

    @Test
    @Override
    void testStream() {
        final ResponseEntity<String> responseEntityString = restClient.post()
                .uri("/api/todo/" + UUID.randomUUID() + "/stream")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(outputStream -> {
                    outputStream.write("From Client: Hello World".getBytes(StandardCharsets.UTF_8));
                    outputStream.flush();
                })
                .retrieve()
                .toEntity(String.class);

        assertEquals(HttpStatus.OK.value(), responseEntityString.getStatusCode().value());
        assertNull(responseEntityString.getBody());

        final String message = restClient.get()
                .uri("/api/todo/" + UUID.randomUUID() + "/stream")
                .accept(MediaType.APPLICATION_OCTET_STREAM)
                .exchange((request, response) -> new String(response.getBody().readAllBytes(), StandardCharsets.UTF_8));
        assertNotNull(message);
        assertEquals("From Server: Hello World", message);
    }
}
