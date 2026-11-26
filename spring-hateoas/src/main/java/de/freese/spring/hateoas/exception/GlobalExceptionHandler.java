package de.freese.spring.hateoas.exception;

import java.net.URI;
import java.nio.file.AccessDeniedException;
import java.util.List;
import java.util.Map;

import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ServerWebExchange;

import de.freese.spring.hateoas.GreetingController;

/**
 * @author Thomas Freese
 * @since 01.12.2017
 */
@RestControllerAdvice(assignableTypes = GreetingController.class)
public class GlobalExceptionHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private static String lastNode(final Path path) {
        String last = null;

        for (final Path.Node node : path) {
            last = node.getName();
        }

        return last;
    }

    @ExceptionHandler
    public ResponseEntity<ProblemDetail> handle(final Exception ex, final ServerWebExchange exchange) {
        LOGGER.error(ex.getMessage(), ex);

        return buildResponseEntity(
                HttpStatus.INTERNAL_SERVER_ERROR,
                URI.create("/problems/internal-error"),
                "Internal server error",
                "Unexpected error",
                null,
                exchange);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ProblemDetail> handleAccessDeniedException(final AccessDeniedException ex, final ServerWebExchange exchange) {
        LOGGER.error(ex.getMessage(), ex);

        return buildResponseEntity(
                HttpStatus.NOT_FOUND,
                URI.create("/problems/security-error"),
                "Access denied",
                ex.getMessage(),
                null,
                exchange);
    }

    // @ExceptionHandler(AuthenticationException.class)
    // public ResponseEntity<ProblemDetail> handleAuthenticationException(final AuthenticationException ex, final ServerWebExchange exchange) {
    //     LOGGER.error(ex.getMessage(), ex);
    //
    //     return buildResponseEntity(
    //             HttpStatus.UNAUTHORIZED,
    //             URI.create("/problems/security-error"),
    //             "Unauthorized",
    //             ex.getMessage(),
    //             null,
    //             exchange);
    // }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ProblemDetail> handleConstraintViolation(final ConstraintViolationException ex, final ServerWebExchange exchange) {
        LOGGER.warn(ex.getMessage(), ex);

        final List<Map<String, String>> errors = ex.getConstraintViolations().stream()
                .map(cv -> Map.of(
                        "field", String.valueOf(lastNode(cv.getPropertyPath())),
                        "message", cv.getMessage()))
                .toList();

        return buildResponseEntity(
                HttpStatus.BAD_REQUEST,
                URI.create("/problems/validation-error"),
                "Validation failed",
                "Input validation failed",
                errors,
                exchange);
    }

    @ExceptionHandler(GreetingException.class)
    public ResponseEntity<ProblemDetail> handleGreetingException(final GreetingException ex, final ServerWebExchange exchange) {
        LOGGER.error(ex.getMessage(), ex);

        return buildResponseEntity(
                HttpStatus.BAD_REQUEST,
                URI.create("/problems/internal-error"),
                "A Problem occurred",
                ex.getMessage(),
                null,
                exchange);
    }

    @ExceptionHandler(ObjectNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleNotFound(final ObjectNotFoundException ex, final ServerWebExchange exchange) {
        LOGGER.error(ex.getMessage(), ex);

        return buildResponseEntity(
                HttpStatus.NOT_FOUND,
                URI.create("/problems/not-found"),
                "Resource not found",
                ex.getMessage(),
                null,
                exchange);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetail> handleValidation(final MethodArgumentNotValidException ex, final ServerWebExchange exchange) {
        LOGGER.warn(ex.getMessage(), ex);

        final List<Map<String, String>> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> Map.of(
                        "field", fe.getField(),
                        "message", String.valueOf(fe.getDefaultMessage())))
                .toList();

        return buildResponseEntity(
                HttpStatus.BAD_REQUEST,
                URI.create("/problems/validation-error"),
                "Validation failed",
                "Input validation failed",
                errors,
                exchange);
    }

    private ResponseEntity<ProblemDetail> buildResponseEntity(final HttpStatus httpStatus,
                                                              final URI type,
                                                              final String title,
                                                              final String detail,
                                                              final List<Map<String, String>> errors,
                                                              final ServerWebExchange webRequest) {
        final ProblemDetail problemDetail = ProblemDetail.forStatus(httpStatus);
        problemDetail.setType(type);
        problemDetail.setTitle(title);
        problemDetail.setDetail(detail);
        problemDetail.setInstance(requestUri(webRequest));

        if (errors != null && !errors.isEmpty()) {
            problemDetail.setProperty("errors", errors);
        }

        return new ResponseEntity<>(problemDetail, httpStatus);
    }

    private URI requestUri(final ServerWebExchange exchange) {
        return exchange.getRequest().getURI();
    }
}
