package de.spring.angular.repository;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import de.spring.angular.model.Task;
import org.springframework.stereotype.Repository;

@Repository
public final class TaskRepository {

    private static final Map<Long, Task> TASKS = new ConcurrentHashMap<>();
    private static final AtomicLong TASKS_SEQUENCE = new AtomicLong(0);

    public Optional<Task> deleteById(final long id) {
        return Optional.ofNullable(TASKS.remove(id));
    }

    public List<Task> findAll() {
        // if (System.currentTimeMillis() % 2 == 0) {
        //     throw new UnsupportedOperationException("Simulated error for testing purposes");
        // }

        return TASKS.values().stream()
                .sorted(Comparator.comparingLong(Task::id))
                .toList();
    }

    public Optional<Task> findById(final long id) {
        return Optional.ofNullable(TASKS.get(id));
    }

    public Task save(final Task task) {
        final long id = TASKS_SEQUENCE.incrementAndGet();

        final Task savedTask = new Task(id, task.title().strip(), task.createdAt());
        TASKS.put(id, savedTask);

        return savedTask;
    }

    public void update(final Task task) {
        TASKS.put(task.id(), task);
    }
}

