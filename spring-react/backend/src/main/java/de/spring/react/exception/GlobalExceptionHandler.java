package de.spring.react.exception;

import java.net.URI;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

/**
 * @author Thomas Freese
 * @since 16.10.26
 */
@RestControllerAdvice
public final class GlobalExceptionHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private static String lastNode(final Path path) {
        String last = null;

        for (final Path.Node node : path) {
            last = node.getName();
        }

        return last;
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ProblemDetail> handleConstraintViolation(final ConstraintViolationException ex, final WebRequest request) {
        LOGGER.error(ex.getMessage(), ex);

        // final List<ApiError.FieldError> fieldErrors = ex.getConstraintViolations().stream()
        //         .map(cv -> new ApiError.FieldError(lastNode(cv.getPropertyPath()), cv.getMessage()))
        //         .toList();
        //
        // return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        //         .body(new ApiError("VALIDATION_ERROR", "Wrong input.", fieldErrors));

        final String detail = ex.getConstraintViolations().stream()
                .map(cv -> lastNode(cv.getPropertyPath()) + ": " + cv.getMessage())
                .collect(Collectors.joining(System.lineSeparator()));

        return buildResponseEntity(HttpStatus.BAD_REQUEST, "Wrong input.", detail, request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleGeneric(final Exception ex, final WebRequest request) {
        LOGGER.error(ex.getMessage(), ex);

        return buildResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage(), null, request);
    }

    @ExceptionHandler(ObjectNotFoundException.class)
    public ResponseEntity<String> handleNotFound(final ObjectNotFoundException ex, final WebRequest request) {
        LOGGER.error(ex.getMessage(), ex);

        // return ResponseEntity.status(HttpStatus.NOT_FOUND)
        //         .body(ApiError.of("NOT_FOUND", ex.getMessage()));
        // return buildResponseEntity(HttpStatus.NOT_FOUND, "NOT_FOUND", ex.getMessage(), request);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<String> handleValidation(final MethodArgumentNotValidException ex, final WebRequest request) {
        LOGGER.error(ex.getMessage(), ex);

        // final List<ApiError.FieldError> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
        //         .map(fe -> new ApiError.FieldError(fe.getField(), fe.getDefaultMessage()))
        //         .toList();
        //
        // return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        //         .body(new ApiError("VALIDATION_ERROR", "Wrong input.", fieldErrors));

        final String detail = ex.getBindingResult().getFieldErrors().stream()
                // .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .collect(Collectors.joining(System.lineSeparator()));

        // return buildResponseEntity(HttpStatus.BAD_REQUEST, "Wrong input.", detail, request);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(detail);
    }

    private ResponseEntity<ProblemDetail> buildResponseEntity(final HttpStatus httpStatus, final Throwable ex, final WebRequest webRequest) {
        final StringBuilder sb = new StringBuilder();
        sb.append(ex.getClass().getSimpleName()).append(": ").append(ex.getMessage());

        // Keep N Elements.
        final StackTraceElement[] stackTraceElements = ex.getStackTrace();

        Stream.of(stackTraceElements)
                .limit(10)
                .map(StackTraceElement::toString)
                .forEach(element -> sb.append("\tat ").append(element));

        if (10 < stackTraceElements.length) {
            sb.append("\t...");
        }

        return buildResponseEntity(httpStatus, ex.getMessage(), sb.toString(), webRequest);
    }

    private ResponseEntity<ProblemDetail> buildResponseEntity(final HttpStatus httpStatus, final String title, final String detail, final WebRequest webRequest) {
        final ProblemDetail problemDetail = ProblemDetail.forStatus(httpStatus);
        problemDetail.setTitle(title);
        problemDetail.setDetail(detail);
        problemDetail.setStatus(httpStatus);

        if (webRequest != null) {
            problemDetail.setType(URI.create(webRequest.getContextPath()));
        }

        return new ResponseEntity<>(problemDetail, httpStatus);
    }
}
