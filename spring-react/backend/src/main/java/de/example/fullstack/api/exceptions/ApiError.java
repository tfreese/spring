package de.example.fullstack.api.exceptions;

import java.util.List;

import org.springframework.http.ProblemDetail;

/**
 * Alternative for {@link ProblemDetail}.
 *
 * @author Thomas Freese
 * @since 16.10.26
 */
public record ApiError(String code, String message, List<FieldError> fieldErrors) {

    public record FieldError(String field, String message) {
    }

    public static ApiError of(final String code, final String message) {
        return new ApiError(code, message, List.of());
    }
}
