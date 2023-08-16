// Created: 16.08.23
package de.freese.spring.data.jpa.domain;

import java.time.LocalDateTime;
import java.util.Objects;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

/**
 * @author Thomas Freese
 */
@Entity
@Table(name = "todo")
public class Todo extends AbstractBaseEntity {

    @NotNull(message = "End Time cannot be blank")
    private LocalDateTime endTime;

    @NotEmpty(message = "Name cannot be blank")
    private String name;

    @NotNull(message = "Start Time cannot be blank")
    private LocalDateTime startTime;

    @Enumerated(EnumType.STRING)
    private Status taskStatus = Status.PENDING;

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof final Todo todo)) {
            return false;
        }

        if (!super.equals(o)) {
            return false;
        }
        
        return Objects.equals(endTime, todo.endTime) && Objects.equals(name, todo.name) && Objects.equals(startTime, todo.startTime) && taskStatus == todo.taskStatus;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public String getName() {
        return name;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public Status getTaskStatus() {
        return taskStatus;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), endTime, name, startTime, taskStatus);
    }

    public void setEndTime(final LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public void setStartTime(final LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public void setTaskStatus(final Status taskStatus) {
        this.taskStatus = taskStatus;
    }
}
