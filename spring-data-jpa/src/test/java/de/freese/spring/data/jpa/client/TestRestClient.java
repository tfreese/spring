// Created: 11.10.2025
package de.freese.spring.data.jpa.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serial;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import jakarta.annotation.Resource;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

import de.freese.spring.data.jpa.domain.Todo;

/**
 * @author Thomas Freese
 */
class TestRestClient extends AbstractClientTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(TestRestClient.class);

    private static final class ProblemDetailErrorHandler implements RestClient.ResponseSpec.ErrorHandler {
        private final JsonMapper jsonMapper;

        private ProblemDetailErrorHandler(final JsonMapper jsonMapper) {
            super();

            this.jsonMapper = Objects.requireNonNull(jsonMapper, "jsonMapper required");
        }

        @Override
        public void handle(final org.springframework.http.HttpRequest request, final ClientHttpResponse response) throws IOException {
            String responseBody = null;

            try (InputStream inputStream = response.getBody()) {
                responseBody = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
            }

            // if (responseBody == null) {
            //     throw new RuntimeException("Failed Response without Error Message");
            // }

            try {
                final ProblemDetail problemDetail = jsonMapper.readValue(responseBody, ProblemDetail.class);

                // jsonMapper.writerWithDefaultPrettyPrinter().writeValue(System.out, problemDetail);

                throw ProblemDetailException.of(problemDetail, jsonMapper);
            }
            catch (ProblemDetailException ex) {
                LOGGER.error(ex.getMessage(), ex);

                throw ex;
            }
            catch (Exception ex) {
                LOGGER.error(ex.getMessage(), ex);

                // Not a ProblemDetail.
                throw new RuntimeException(responseBody);
            }
        }
    }

    private static final class ProblemDetailException extends RuntimeException {
        @Serial
        private static final long serialVersionUID = 6598535554136224024L;

        static ProblemDetailException of(final ProblemDetail problemDetail, final JsonMapper jsonMapper) {
            final StackTraceElement[] stackTrace = jsonMapper.readValue(problemDetail.getDetail(), new TypeReference<>() {
            });

            final ProblemDetailException exception = new ProblemDetailException(problemDetail.getTitle(), problemDetail.getProperties());
            exception.setStackTrace(stackTrace);

            return exception;
        }

        private final transient Map<String, Object> properties;

        private ProblemDetailException(final String message, final Map<String, Object> properties) {
            super(message);

            this.properties = Optional.ofNullable(properties).orElse(Map.of());
        }

        public Map<String, Object> getProperties() {
            return Map.copyOf(properties);
        }

        Object getProperty(final String key) {
            if (properties == null) {
                return null;
            }

            return properties.get(key);
        }
    }

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
    void testProblemDetail() {
        final ProblemDetailException exception = assertThrows(ProblemDetailException.class, () -> restClient
                .get()
                .uri("/api/todo/problemDetail")
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus(HttpStatusCode::isError, new ProblemDetailErrorHandler(getJsonMapper()))
                .toEntity(String.class)
        );

        assertNotNull(exception);
        assertNotNull(exception.getStackTrace());
        assertNotNull(exception.getProperties());
        assertEquals("java.lang.RuntimeException: something went wrong", exception.getMessage());
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
