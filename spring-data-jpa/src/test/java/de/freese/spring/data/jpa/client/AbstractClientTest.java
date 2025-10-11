// Created: 11.10.2025
package de.freese.spring.data.jpa.client;

import java.time.LocalDateTime;

import jakarta.annotation.Resource;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import de.freese.spring.data.jpa.domain.Status;
import de.freese.spring.data.jpa.domain.Todo;

/**
 * @author Thomas Freese
 */
// @SpringBootTest(properties = "server.port=0", webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = ...)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
abstract class AbstractClientTest {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @LocalServerPort
    private int localServerPort;

    @Resource
    private ObjectMapper objectMapper;

    public Logger getLogger() {
        return logger;
    }

    Todo creaTodo() {
        final Todo todo = new Todo();
        todo.setName("Test");
        todo.setStartTime(LocalDateTime.now());
        todo.setEndTime(LocalDateTime.now().plusDays(1));
        todo.setTaskStatus(Status.PENDING);

        return todo;
    }

    ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    String getUrl() {
        return "http://localhost:" + localServerPort + "/spring-data-jpa";
    }

    abstract void testCreateTodo();

    abstract void testGetAllTodosJson();

    abstract void testGetAllTodosXml();

    abstract void testNotFound();

    abstract void testStream();
}
