// Created: 22.08.2024
package de.freese.spring.data.jpa.client;

import java.util.List;
import java.util.UUID;

import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

import de.freese.spring.data.jpa.domain.Todo;

/**
 * @author Thomas Freese
 */
@HttpExchange(url = "/api/todo", accept = MediaType.APPLICATION_JSON_VALUE)
interface TodoClient {

    @PostExchange(contentType = MediaType.APPLICATION_JSON_VALUE)
    Todo createTodo(@RequestBody Todo todo);

    @GetExchange
    List<Todo> getAllTodosJson();

    @GetExchange(accept = MediaType.APPLICATION_XML_VALUE)
    List<Todo> getAllTodosXml();

    @GetExchange(value = "/{id}/stream", accept = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    ResponseEntity<Resource> getStream(@PathVariable("id") UUID id);
    // InputStreamResource getStream(@PathVariable("id") final UUID id);

    @GetExchange(value = "/{id}")
    Todo getTodoById(@PathVariable UUID id) throws HttpClientErrorException.NotFound;

    @PostExchange(value = "/{id}/stream", contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    ResponseEntity<Void> putStream(@PathVariable("id") UUID id, @RequestBody Resource resource);
}
