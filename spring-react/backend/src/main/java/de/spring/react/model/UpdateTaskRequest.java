package de.spring.react.model;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateTaskRequest(
        @NotBlank(message = "Der Titel darf nicht leer sein")
        @Size(max = 30, message = "Der Titel darf maximal 30 Zeichen enthalten")
        String title,
        @NotNull(message = "Das Erstellungsdatum darf nicht leer sein")
        LocalDateTime createdAt
) {
}
