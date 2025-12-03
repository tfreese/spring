// Created: 16.08.23
package de.freese.spring.data.jpa.controller;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import tools.jackson.databind.json.JsonMapper;

import de.freese.spring.data.jpa.domain.Todo;
import de.freese.spring.data.jpa.exception.RestExceptionHandler;
import de.freese.spring.data.jpa.infrastructure.TodoService;

/**
 * @author Thomas Freese
 */
@RestController
@RequestMapping("/api/todo")
public class TodoController {
    private static final Logger LOGGER = LoggerFactory.getLogger(TodoController.class);

    private final JsonMapper jsonMapper;
    private final TodoService todoService;

    @Autowired
    public TodoController(final TodoService todoService, final JsonMapper jsonMapper) {
        super();

        this.todoService = Objects.requireNonNull(todoService, "todoService required");
        this.jsonMapper = Objects.requireNonNull(jsonMapper, "jsonMapper required");
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Todo> createTodo(final @RequestBody Todo todo) {
        final Todo createdTodo = todoService.createTodo(todo);

        return ResponseEntity.status(HttpStatus.CREATED).body(createdTodo);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTodo(final @PathVariable UUID id) {
        todoService.deleteTodo(id);

        return ResponseEntity.noContent().build();
    }

    @GetMapping(produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public List<Todo> getAllTodos() {
        return todoService.getAllTodos();
    }

    @GetMapping(path = "/exception", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> getExceptionWithoutRestExceptionHandler() {
        try {
            throw new RuntimeException("something went wrong");
        }
        catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);

            final ProblemDetail problemDetail = RestExceptionHandler.createProblemDetail(ex, HttpStatus.INTERNAL_SERVER_ERROR, jsonMapper);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(problemDetail);
        }
    }

    /**
     * Handled by {@link RestExceptionHandler}.
     */
    @GetMapping(path = "/problemDetail", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> getProblemDetail() {
        throw new RuntimeException("something went wrong");
    }

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Todo getTodoById(final @PathVariable UUID id) {
        return todoService.getTodoById(id);
    }

    /**
     * Jakarta:<br>
     * <pre>{@code
     * public void test(@PathVariable("id") final UUID id, final InputStream inputStream) throws IOException {}
     * }</pre><br>
     *
     * AtRequestBody final byte[] payload
     */
    @PostMapping(value = "/{id}/stream", consumes = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public void testStream(@PathVariable("id") final UUID id, @RequestBody final InputStreamResource inputStreamResource) throws IOException {
        LOGGER.info("id = {}", id);

        try (InputStream inputStream = inputStreamResource.getInputStream();
             Reader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
             BufferedReader bufferedReader = new BufferedReader(reader)) {
            final String message = bufferedReader.lines().collect(Collectors.joining(System.lineSeparator()));
            LOGGER.info("id = {}, message = {}", id, message);
        }
    }

    /**
     * Jakarta:<br>
     * <pre>{@code
     * public Response test(@PathVariable("id") final UUID id) throws IOException {
     *     return Response.ok((StreamingOutput) outputStream -> {
     *              try (InputStream inputStream = new … {
     *                 inputStream.transferTo(outputStream);
     *                 outputStream.flush();
     *             }
     *         }).build();
     * }
     * // ResponseEntity<byte[]>
     * }</pre>
     *
     * StreamingResponseBody, InputStreamResource working booth alone and with ResponseEntity.<br>
     */
    @GetMapping(value = "/{id}/stream", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public StreamingResponseBody testStream(@PathVariable("id") final UUID id) {
        LOGGER.info("id = {}", id);

        return outputStream -> {
            try (InputStream inputStream = new ByteArrayInputStream("From Server: Hello World".getBytes(StandardCharsets.UTF_8))) {
                inputStream.transferTo(outputStream);
                outputStream.flush();
            }
        };

        // return ResponseEntity.ok()
        //         .contentType(MediaType.APPLICATION_OCTET_STREAM)
        //         .body(outputStream -> {
        //             try (InputStream inputStream = new ByteArrayInputStream("From Server: Hello World".getBytes(StandardCharsets.UTF_8))) {
        //                 inputStream.transferTo(outputStream);
        //                 outputStream.flush();
        //             }
        //         });

        // return new InputStreamResource(new ByteArrayInputStream("From Server: Hello World".getBytes(StandardCharsets.UTF_8)));
    }

    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Todo updateTodo(final @PathVariable UUID id, final @RequestBody Todo todoDetails) {
        return todoService.updateTodo(id, todoDetails);
    }
}
