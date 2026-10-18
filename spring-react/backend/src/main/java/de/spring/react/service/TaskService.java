package de.spring.react.service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import jakarta.annotation.PostConstruct;

import de.spring.react.model.CreateTaskRequest;
import de.spring.react.model.Task;
import de.spring.react.model.UpdateTaskRequest;
import de.spring.react.repository.TaskRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * @author Thomas Freese
 * @since 17.10.26
 */
@Service
public final class TaskService {
    private static final Logger LOGGER = LoggerFactory.getLogger(TaskService.class);

    private final TaskRepository taskRepository;

    public TaskService(final TaskRepository taskRepository) {
        super();

        this.taskRepository = Objects.requireNonNull(taskRepository, "taskRepository required");
    }

    public Optional<Task> deleteById(final long id) {
        LOGGER.info("Delete task with id: {}", id);

        return taskRepository.deleteById(id);
    }

    public List<Task> findAll() {
        return taskRepository.findAll();
    }

    public Optional<Task> findById(final long id) {
        return taskRepository.findById(id);
    }

    public Task insert(final CreateTaskRequest createTaskRequest) {
        LOGGER.info("Inserting new task: {}", createTaskRequest);

        final Task task = new Task(-1L, createTaskRequest.title().strip(), LocalDateTime.now(ZoneId.systemDefault()));

        final Task created = taskRepository.save(task);

        LOGGER.info("Task inserted successfully: {}", created);

        return created;
    }

    public Optional<Task> update(final long id, final UpdateTaskRequest updateTaskRequest) {
        LOGGER.info("Updating task: {}", updateTaskRequest);

        final Optional<Task> task = taskRepository.findById(id);

        if (task.isPresent()) {
            final LocalDateTime createdAt = updateTaskRequest.createdAt() != null ? updateTaskRequest.createdAt() : task.get().createdAt();
            final Task updatedTask = new Task(id, updateTaskRequest.title(), createdAt);

            taskRepository.update(updatedTask);

            LOGGER.info("Task updated successfully: {}", updatedTask);

            return Optional.of(updatedTask);
        }

        LOGGER.warn("Task not found: {}", id);

        return Optional.empty();
    }

    @PostConstruct
    void initSampleTasks() {
        LOGGER.info("Initializing sample tasks...");

        taskRepository.save(new Task(-1L, "X-Mas", LocalDateTime.of(2026, 12, 24, 0, 0, 0, 0)));
        taskRepository.save(new Task(-1L, "Dinner", LocalDateTime.of(2026, 12, 25, 12, 30, 0, 0)));
        taskRepository.save(new Task(-1L, "New Year", LocalDateTime.of(2026, 12, 31, 23, 59, 59, 999_999_999)));
    }
}
