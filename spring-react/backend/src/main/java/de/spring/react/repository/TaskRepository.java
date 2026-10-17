package de.spring.react.repository;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import de.spring.react.model.Task;
import org.springframework.stereotype.Repository;

@Repository
public final class TaskRepository {

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

    public Task save(final Task task) {
        final long id = tasks.size() + 1L;

        final Task savedTask = new Task(id, task.title().strip(), task.createdAt());
        tasks.put(id, savedTask);

        return savedTask;
    }

    public void update(final Task task) {
        tasks.put(task.id(), task);
    }
}
