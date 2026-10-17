package de.spring.react.repository;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import jakarta.annotation.PostConstruct;

import de.spring.react.model.Task;
import org.springframework.stereotype.Repository;

@Repository
public class TaskRepository {

    private final AtomicLong sequence = new AtomicLong();
    private final Map<Long, Task> tasks = new ConcurrentHashMap<>();

    public Optional<Task> deleteById(final long id) {
        return Optional.ofNullable(tasks.remove(id));
    }

    public List<Task> findAll() {
        // if (System.currentTimeMillis() % 2 == 0) {
        //     throw new UnsupportedOperationException("Simulated error for testing purposes");
        // }

        return tasks.values().stream()
                .sorted(Comparator.comparingLong(Task::id))
                .toList();
    }

    public Optional<Task> findById(final long id) {
        return Optional.ofNullable(tasks.get(id));
    }

    @PostConstruct
    public void initializeSampleTasks() {
        save("Spring-Boot-Backend starten");
        save("React-Frontend laden");
    }

    public Task save(final String title) {
        final long id = sequence.incrementAndGet();

        final Task task = new Task(id, title.strip(), LocalDateTime.now(ZoneId.systemDefault()));
        tasks.put(id, task);

        return task;
    }

    public void update(final Task task) {
        tasks.put(task.id(), task);
    }
}
