package de.spring.angular.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateTaskRequest(
        @NotBlank(message = "Der Titel darf nicht leer sein")
        @Size(max = 60, message = "Der Titel darf maximal 60 Zeichen enthalten")
        String title
) {
}

