// Created: 16.08.23
package de.freese.spring.data.jpa.web;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
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

import de.freese.spring.data.jpa.domain.Todo;
import de.freese.spring.data.jpa.infrastructure.TodoService;

/**
 * @author Thomas Freese
 */
@RestController
@RequestMapping("/api/todo")
public class TodoController {

    private final TodoService todoService;

    @Autowired
    TodoController(final TodoService todoService) {
        this.todoService = todoService;
    }

    @PostMapping
    public ResponseEntity<Todo> createTodo(final @RequestBody Todo todo) {
        final Todo createdTodo = todoService.createTodo(todo);

        return ResponseEntity.status(HttpStatus.CREATED).body(createdTodo);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(final @PathVariable UUID id) {
        todoService.deleteTodo(id);

        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<Todo>> getAllTodos() {
        final List<Todo> todos = todoService.getAllTodos();

        return ResponseEntity.ok(todos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Todo> getTodoById(final @PathVariable UUID id) {
        final Todo product = todoService.getTodoById(id);

        return ResponseEntity.ok(product);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Todo> updateTodo(final @PathVariable UUID id, final @RequestBody Todo todoDetails) {
        final Todo updatedTodo = todoService.updateTodo(id, todoDetails);

        return ResponseEntity.ok(updatedTodo);
    }
}
