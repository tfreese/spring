// Created: 03.09.2018
package de.freese.spring.thymeleaf.exception;

import java.util.Optional;
import java.util.stream.Collectors;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * @author Thomas Freese
 */
// @Order(Ordered.HIGHEST_PRECEDENCE)
@ControllerAdvice(annotations = RestController.class)
// @ControllerAdvice
// @RestControllerAdvice
// @Controller
public class RestControllerExceptionHandler extends ResponseEntityExceptionHandler {
    protected static Exception getException(final HttpServletRequest request) {
        return (Exception) request.getAttribute(RequestDispatcher.ERROR_EXCEPTION);
    }

    protected static Exception getException(final WebRequest request) {
        final HttpServletRequest httpServletRequest = getHttpServletRequest(request);

        return getException(httpServletRequest);
    }

    protected static HttpServletRequest getHttpServletRequest(final WebRequest request) {
        return (HttpServletRequest) ((ServletWebRequest) request).getNativeRequest();
    }

    protected static String getPath(final HttpServletRequest request) {
        return request.getRequestURL().toString();
    }

    protected static String getPath(final WebRequest request) {
        final HttpServletRequest httpServletRequest = getHttpServletRequest(request);

        return getPath(httpServletRequest);
    }

    protected static HttpStatus getStatus(final HttpServletRequest request) {
        final Integer statusCode = (Integer) request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);

        if (statusCode == null) {
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }

        try {
            return HttpStatus.valueOf(statusCode);
        }
        catch (Exception ex) {
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }
    }

    protected static HttpStatus getStatus(final WebRequest request) {
        final HttpServletRequest httpServletRequest = getHttpServletRequest(request);

        return getStatus(httpServletRequest);
    }

    private static StringBuilder getDetail(final Throwable ex) {
        //        StringWriter sw = new StringWriter();
        //
        //        try (PrintWriter pw = new PrintWriter(sw, false)) {
        //            pw.println(ex);
        //            ex.printStackTrace(pw);
        //        }
        //
        //        return sw.toString();

        final StringBuilder sb = new StringBuilder();
        sb.append(ex.getClass().getSimpleName()).append(": ").append(ex.getMessage());

        // Keep last N Elements.
        final int keepLastNStaceTraceElements = 4;

        final StackTraceElement[] stackTraceOrigin = ex.getStackTrace();
        StackTraceElement[] stackTrace = stackTraceOrigin;

        if (stackTraceOrigin.length > keepLastNStaceTraceElements) {
            stackTrace = new StackTraceElement[keepLastNStaceTraceElements];
            System.arraycopy(stackTraceOrigin, 0, stackTrace, 0, keepLastNStaceTraceElements);
        }

        for (StackTraceElement stackTraceElement : stackTrace) {
            sb.append("\tat ").append(stackTraceElement);
        }

        if (stackTraceOrigin.length > keepLastNStaceTraceElements) {
            sb.append("\t...");
        }

        return sb;
    }

    public RestControllerExceptionHandler() {
        super();
    }

    protected ResponseEntity<Object> buildResponseEntity(final Throwable ex, final WebRequest request, final String message, final HttpStatus httpStatus) {
        this.logger.error(ex.getLocalizedMessage());

        final ProblemDetail problemDetail = ProblemDetail.forStatus(httpStatus);
        problemDetail.setTitle(ex.getMessage());
        problemDetail.setDetail(getDetail(ex).toString());
        problemDetail.setStatus(httpStatus);
        // problemDetail.setType(URI.create(request.getContextPath()));

        return new ResponseEntity<>(problemDetail, httpStatus);
    }

    @ExceptionHandler(value = AccessDeniedException.class)
    protected ResponseEntity<Object> handleAccessDeniedException(final AccessDeniedException ex, final WebRequest request) {
        return buildResponseEntity(ex, request, "Access Denied", HttpStatus.FORBIDDEN);
    }

    /**
     * Handles jakarta.validation.ConstraintViolationException. Thrown when @Validated fails.
     */
    @ExceptionHandler(ConstraintViolationException.class)
    protected ResponseEntity<Object> handleConstraintViolation(final ConstraintViolationException ex, final WebRequest request) {
        final String message = ex.getConstraintViolations().stream().map(ConstraintViolation::toString).collect(Collectors.joining(","));

        return buildResponseEntity(ex, request, message, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Throwable.class)
    protected ResponseEntity<Object> handleGenericException(final Throwable ex, final WebRequest request) {
        ResponseEntity<Object> responseEntity = null;

        try {
            if (ex instanceof Exception e) {
                responseEntity = handleException(e, request);
            }
        }
        catch (Exception ex2) {
            // Empty
        }

        if (responseEntity == null) {
            responseEntity = buildResponseEntity(ex, request, "Unhandled Exception: " + ex.getClass().getSimpleName(), HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return responseEntity;
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    protected ResponseEntity<Object> handleMethodArgumentTypeMismatch(final MethodArgumentTypeMismatchException ex, final WebRequest request) {
        final String message = "The parameter '%s' of value '%s' could not be converted to type '%s'".formatted(
                ex.getName(),
                ex.getValue(),
                Optional.ofNullable(ex.getRequiredType()).map(Class::getSimpleName).orElse("unknown")
        );

        return buildResponseEntity(ex, request, message, HttpStatus.BAD_REQUEST);
    }
}
