// Created: 16.08.23
package de.freese.spring.data.jpa.infrastructure;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import de.freese.spring.data.jpa.domain.Todo;

/**
 * @author Thomas Freese
 */
public interface TodoRepository extends JpaRepository<Todo, UUID> {
}
