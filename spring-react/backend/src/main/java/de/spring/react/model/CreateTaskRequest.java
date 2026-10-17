package de.spring.react.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateTaskRequest(
        @NotBlank(message = "Der Titel darf nicht leer sein")
        @Size(max = 30, message = "Der Titel darf maximal 30 Zeichen enthalten")
        String title
) {
}
