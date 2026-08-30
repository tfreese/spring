package de.freese.spring.data.jpa.infrastructure;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import de.freese.spring.data.jpa.domain.Todo;
import de.freese.spring.data.jpa.exception.ObjectNotFoundException;

/**
 * @author Thomas Freese
 * @since 16.08.2023
 */
@Service
public class TodoService {
    private final TodoRepository repository;

    TodoService(final TodoRepository repository) {
        super();

        this.repository = repository;
    }

    public Todo createTodo(final Todo todo) {
        return repository.save(todo);
    }

    public void deleteTodo(final UUID id) {
        repository.deleteById(id);
    }

    public List<Todo> getAllTodos() {
        return repository.findAll();
    }

    public Todo getTodoById(final UUID id) {
        return repository.findById(id).orElseThrow(() -> new ObjectNotFoundException(Todo.class, id));
    }

    public Todo updateTodo(final UUID id, final Todo todoDetail) {
        final Todo todo = getTodoById(id);

        todo.setName(todoDetail.getName());
        todo.setStartTime(todoDetail.getStartTime());
        todo.setEndTime(todoDetail.getEndTime());
        todo.setTaskStatus(todoDetail.getTaskStatus());

        return repository.save(todo);
    }
}
