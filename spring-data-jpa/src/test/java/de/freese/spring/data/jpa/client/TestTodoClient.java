// Created: 11.10.2025
package de.freese.spring.data.jpa.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpExchangeAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

import de.freese.spring.data.jpa.domain.Todo;

/**
 * @author Thomas Freese
 */
class TestTodoClient extends AbstractClientTest {
    private static TodoClient todoClient;

    @BeforeAll
    static void beforeAll(@LocalServerPort final int localServerPort) {
        final RestClient restClient = RestClient.builder()
                .baseUrl("http://localhost:" + localServerPort + "/spring-data-jpa")
                // .defaultStatusHandler(HttpStatusCode::isError, (request, response) -> {
                //             final URI uri = request.getURI();
                //             String message = null;
                //
                //             // Extract ProblemDetail and create Client Exception.
                //
                //             // Causes Exception "Stream already closed" in further processing.
                //             try (InputStream inputStream = response.getBody()) {
                //                 message = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
                //             }
                //
                //             LOGGER.error("{}: {}", uri, message);
                //         }
                // )
                // .defaultStatusHandler(new DefaultResponseErrorHandler() {
                //     @Override
                //     protected void handleError(final ClientHttpResponse response, final HttpStatusCode statusCode, final URI url, final HttpMethod method) throws IOException {
                //         // super.handleError(response, statusCode, url, method);
                //
                //         // Extract ProblemDetail and create Client Exception.
                //
                //         final String statusText = response.getStatusText();
                //         final byte[] body = getResponseBody(response);
                //         final Charset responseCharset = getCharset(response);
                //
                //         final Charset charset = responseCharset != null ? responseCharset : StandardCharsets.UTF_8;
                //
                //         String message = "[no body]";
                //
                //         if (body != null && body.length > 0) {
                //             message = new String(body, charset);
                //         }
                //
                //         LOGGER.error("{} {} on {} request for '{}': {}", statusCode.value(), statusText, method, url, message);
                //     }
                // })
                .build();

        final HttpExchangeAdapter httpExchangeAdapter = RestClientAdapter.create(restClient);

        final HttpServiceProxyFactory proxyFactory = HttpServiceProxyFactory.builderFor(httpExchangeAdapter).build();

        todoClient = proxyFactory.createClient(TodoClient.class);
    }

    @Test
    @Override
    void testCreateTodo() {
        final Todo todo = creaTodo();

        final Todo todoResponse = todoClient.createTodo(todo);
        assertNotNull(todoResponse);
        assertEquals(todo.getName(), todoResponse.getName());
        assertEquals(todo.getStartTime(), todoResponse.getStartTime());
        assertEquals(todo.getEndTime(), todoResponse.getEndTime());
        assertEquals(todo.getTaskStatus(), todoResponse.getTaskStatus());
    }

    @Test
    @Override
    void testGetAllTodosJson() {
        testCreateTodo();

        final List<Todo> todos = todoClient.getAllTodosJson();
        assertNotNull(todos);
        assertFalse(todos.isEmpty());
    }

    @Test
    @Override
    void testGetAllTodosXml() {
        testCreateTodo();

        final List<Todo> todos = todoClient.getAllTodosXml();
        assertNotNull(todos);
        assertFalse(todos.isEmpty());
    }

    @Test
    @Override
    void testNotFound() {
        final HttpClientErrorException exception = assertThrows(HttpClientErrorException.NotFound.class, () -> todoClient.getTodoById(UUID.randomUUID()));

        assertNotNull(exception);

        getLogger().error(exception.getResponseBodyAsString());

        assertEquals(HttpStatus.NOT_FOUND.value(), exception.getStatusCode().value());
        assertTrue(exception.getResponseBodyAsString().contains("Todo not found by ID:"));
    }

    @Test
    @Override
    void testStream() {
        try {
            try (InputStream inputStream = new ByteArrayInputStream("From Client: Hello World".getBytes(StandardCharsets.UTF_8))) {
                final ResponseEntity<Void> responseEntity = todoClient.putStream(UUID.randomUUID(), new InputStreamResource(inputStream));

                assertNotNull(responseEntity);
                assertEquals(HttpStatus.OK.value(), responseEntity.getStatusCode().value());
            }

            // ByteArrayResource NOT InputStreamResource !!!
            // final InputStreamResource inputStreamResource = todoClient.getStream(UUID.randomUUID());
            // assertNotNull(inputStreamResource);

            final ResponseEntity<org.springframework.core.io.Resource> responseEntity = todoClient.getStream(UUID.randomUUID());
            assertNotNull(responseEntity);
            assertEquals(HttpStatus.OK.value(), responseEntity.getStatusCode().value());

            final org.springframework.core.io.Resource resource = responseEntity.getBody();
            // final org.springframework.core.io.Resource resource = inputStreamResource;

            try (InputStream inputStream = resource.getInputStream()) {
                final String message = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
                assertEquals("From Server: Hello World", message);
                getLogger().info(message);
            }
        }
        catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }
}
