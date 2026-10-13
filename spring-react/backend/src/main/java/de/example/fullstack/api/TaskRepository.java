package de.example.fullstack.api;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import jakarta.annotation.PostConstruct;

import org.springframework.stereotype.Repository;

@Repository
public class TaskRepository {

    private final AtomicLong sequence = new AtomicLong();
    private final Map<Long, Task> tasks = new ConcurrentHashMap<>();

    public Optional<Task> deleteById(final long id) {
        return Optional.ofNullable(tasks.remove(id));
    }

    public List<Task> findAll() {
        if (System.currentTimeMillis() % 2 == 0) {
            throw new UnsupportedOperationException("Simulated error for testing purposes");
        }

        return tasks.values().stream()
                .sorted(Comparator.comparingLong(Task::id))
                .toList();
    }

    @PostConstruct
    public void initializeSampleTasks() {
        save("Spring-Boot-Backend starten");
        save("React-Frontend laden");
    }

    public Task save(final String title) {
        final long id = sequence.incrementAndGet();

        final Task task = new Task(id, title.strip());
        tasks.put(id, task);

        return task;
    }
}
