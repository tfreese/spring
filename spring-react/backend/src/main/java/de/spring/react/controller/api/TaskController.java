package de.spring.react.controller.api;

import java.net.URI;
import java.util.List;
import java.util.Objects;

import jakarta.validation.Valid;

import de.spring.react.exception.ObjectNotFoundException;
import de.spring.react.model.CreateTaskRequest;
import de.spring.react.model.Task;
import de.spring.react.model.UpdateTaskRequest;
import de.spring.react.service.TaskService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {
    private static final Logger LOGGER = LoggerFactory.getLogger(TaskController.class);

    private final TaskService taskService;

    public TaskController(final TaskService taskService) {
        super();

        this.taskService = Objects.requireNonNull(taskService, "taskService required");
    }

    @PostMapping
    public ResponseEntity<Task> create(@Valid @RequestBody final CreateTaskRequest createTaskRequest, final UriComponentsBuilder uriBuilder) {
        LOGGER.info("Received request to create task: {}", createTaskRequest);

        final Task created = taskService.insert(createTaskRequest);

        final URI location = uriBuilder.path("/api/tasks/{id}").build(created.id());

        return ResponseEntity.created(location).body(created);
    }

    @DeleteMapping("/{id}")
    // @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<Void> delete(@PathVariable final long id) {
        LOGGER.info("Received request to delete task: {}", id);

        return taskService.deleteById(id)
                .map(ignored -> ResponseEntity.noContent().<Void>build())
                .orElseThrow(() -> new ObjectNotFoundException(Task.class, id));
    }

    @GetMapping
    public ResponseEntity<List<Task>> findAll() {
        LOGGER.info("Received request to find all tasks");

        try {
            return ResponseEntity.ok(taskService.findAll());
        }
        catch (final Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Task> findById(@PathVariable final long id) {
        LOGGER.info("Received request to find task: {}", id);

        return taskService.findById(id)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ObjectNotFoundException(Task.class, id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> update(@PathVariable final long id, @Valid @RequestBody final UpdateTaskRequest updateTaskRequest) {
        LOGGER.info("Received request to update task: {} - {}", id, updateTaskRequest);

        return taskService.update(id, updateTaskRequest)
                .map(ignored -> ResponseEntity.ok().<Void>build())
                .orElseThrow(() -> new ObjectNotFoundException(Task.class, id));
    }
}
