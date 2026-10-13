package de.example.fullstack.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateTaskRequest(
        @NotBlank(message = "Der Titel darf nicht leer sein")
        @Size(max = 200, message = "Der Titel darf maximal 200 Zeichen enthalten")
        String title
) {
}
