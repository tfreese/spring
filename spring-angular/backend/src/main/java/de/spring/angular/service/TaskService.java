package de.spring.angular.service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import jakarta.annotation.PostConstruct;

import de.spring.angular.model.CreateTaskRequest;
import de.spring.angular.model.Task;
import de.spring.angular.model.UpdateTaskRequest;
import de.spring.angular.repository.TaskRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

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

        taskRepository.save(new Task(-1L, "Weihnachten", LocalDateTime.of(2026, 12, 24, 0, 0, 0, 0)));
        taskRepository.save(new Task(-1L, "Essen", LocalDateTime.of(2026, 12, 25, 12, 30, 0, 0)));
        taskRepository.save(new Task(-1L, "Neujahr", LocalDateTime.of(2026, 12, 31, 23, 59, 59, 999_999_999)));
        taskRepository.save(new Task(-1L, "Wocheneinkauf erledigen", LocalDateTime.of(2026, 11, 4, 17, 0)));
        taskRepository.save(new Task(-1L, "Arzttermin vereinbaren", LocalDateTime.of(2026, 11, 5, 9, 0)));
        taskRepository.save(new Task(-1L, "Fahrrad zur Inspektion bringen", LocalDateTime.of(2026, 11, 6, 8, 30)));
        taskRepository.save(new Task(-1L, "Wohnung reinigen", LocalDateTime.of(2026, 11, 7, 10, 0)));
        taskRepository.save(new Task(-1L, "Familienbesuch planen", LocalDateTime.of(2026, 11, 8, 15, 0)));
        taskRepository.save(new Task(-1L, "Projektbericht fertigstellen", LocalDateTime.of(2026, 11, 9, 14, 0)));
        taskRepository.save(new Task(-1L, "Datensicherung erstellen", LocalDateTime.of(2026, 11, 10, 18, 0)));
        taskRepository.save(new Task(-1L, "Versicherungen vergleichen", LocalDateTime.of(2026, 11, 12, 19, 0)));
        taskRepository.save(new Task(-1L, "Winterreifen wechseln", LocalDateTime.of(2026, 11, 14, 9, 30)));
        taskRepository.save(new Task(-1L, "Unterlagen sortieren", LocalDateTime.of(2026, 11, 15, 11, 0)));
        taskRepository.save(new Task(-1L, "Teammeeting vorbereiten", LocalDateTime.of(2026, 11, 17, 8, 0)));
        taskRepository.save(new Task(-1L, "Bibliotheksbuch abgeben", LocalDateTime.of(2026, 11, 20, 16, 30)));
        taskRepository.save(new Task(-1L, "Adventskalender besorgen", LocalDateTime.of(2026, 11, 28, 10, 0)));
        taskRepository.save(new Task(-1L, "Geschenke besorgen", LocalDateTime.of(2026, 12, 5, 11, 0)));
        taskRepository.save(new Task(-1L, "Weihnachtspost verschicken", LocalDateTime.of(2026, 12, 14, 9, 0)));
        taskRepository.save(new Task(-1L, "Festessen vorbereiten", LocalDateTime.of(2026, 12, 23, 16, 0)));
        taskRepository.save(new Task(-1L, "Jahresziele planen", LocalDateTime.of(2027, 1, 2, 10, 0)));
    }
}
