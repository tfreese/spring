package de.example.fullstack.api;

import java.net.URI;
import java.util.List;

import jakarta.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {
    private static final Logger LOGGER = LoggerFactory.getLogger(TaskController.class);

    private final TaskRepository repository;

    public TaskController(final TaskRepository repository) {
        super();

        this.repository = repository;
    }

    @PostMapping
    public ResponseEntity<Task> create(@Valid @RequestBody final CreateTaskRequest request, final UriComponentsBuilder uriBuilder) {
        LOGGER.info("Received request to create task {}", request);

        final Task created = repository.save(request.title());
        final URI location = uriBuilder.path("/api/tasks/{id}").build(created.id());

        return ResponseEntity.created(location).body(created);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable final long id) {
        LOGGER.info("Received request to delete task {}", id);

        return repository.deleteById(id)
                .map(ignored -> ResponseEntity.noContent().<Void>build())
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // @GetMapping
    // @ResponseStatus(HttpStatus.OK)
    // public List<Task> findAll() {
    //     LOGGER.info("Received request to find all tasks");
    //
    //     return repository.findAll();
    // }
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<List<Task>> findAll() {
        LOGGER.info("Received request to find all tasks");

        try {
            return ResponseEntity.ok(repository.findAll());
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
