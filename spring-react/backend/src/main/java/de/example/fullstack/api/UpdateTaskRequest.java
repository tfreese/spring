package de.example.fullstack.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateTaskRequest(
        @NotBlank(message = "Der Titel darf nicht leer sein")
        @Size(max = 20, message = "Der Titel darf maximal 20 Zeichen enthalten")
        String title
) {
}
