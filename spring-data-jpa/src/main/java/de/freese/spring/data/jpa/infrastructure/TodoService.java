// Created: 16.08.23
package de.freese.spring.data.jpa.infrastructure;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import de.freese.spring.data.jpa.domain.Todo;

/**
 * @author Thomas Freese
 */
@Service
public class TodoService {

    private final TodoRepository repository;

    TodoService(TodoRepository repository) {
        super();

        this.repository = repository;
    }

    public Todo createTodo(Todo todo) {
        return repository.save(todo);
    }

    public void deleteTodo(UUID id) {
        repository.deleteById(id);
    }

    public List<Todo> getAllTodos() {
        return repository.findAll();
    }

    public Todo getTodoById(UUID id) {
        return repository.findById(id).orElseThrow(() -> new IllegalArgumentException("Todo not found"));
    }

    public Todo updateTodo(UUID id, Todo todoDetail) {
        Todo todo = repository.findById(id).orElseThrow(() -> new IllegalArgumentException("Todo not found"));

        todo.setName(todoDetail.getName());
        todo.setStartTime(todoDetail.getStartTime());
        todo.setEndTime(todoDetail.getEndTime());
        todo.setTaskStatus(todoDetail.getTaskStatus());

        return repository.save(todo);
    }
}