package de.example.fullstack.api;

import java.net.URI;
import java.util.List;
import java.util.Optional;

import jakarta.validation.Valid;

import de.example.fullstack.api.exceptions.ObjectNotFoundException;
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
                .map(ignored -> ResponseEntity.ok().<Void>build())
                .orElseThrow(() -> new ObjectNotFoundException(Task.class, id));
    }

    // @GetMapping
    // @ResponseStatus(HttpStatus.OK)
    // public List<Task> findAll() {
    //     LOGGER.info("Received request to find all tasks");
    //
    //     return repository.findAll();
    // }
    @GetMapping
    public ResponseEntity<List<Task>> findAll() {
        LOGGER.info("Received request to find all tasks");

        try {
            return ResponseEntity.ok(repository.findAll());
        }
        catch (final Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Task> findById(@PathVariable final long id) {
        LOGGER.info("Received request to find task {}", id);

        return repository.findById(id)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ObjectNotFoundException(Task.class, id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> update(@PathVariable final long id, @Valid @RequestBody final UpdateTaskRequest request) {
        LOGGER.info("Received request to update task {} - {}", id, request);

        final Optional<Task> task = repository.findById(id);

        if (task.isPresent()) {
            repository.update(new Task(id, request.title()));
            return ResponseEntity.ok().build();
        }

        throw new ObjectNotFoundException(Task.class, id);
    }
}
