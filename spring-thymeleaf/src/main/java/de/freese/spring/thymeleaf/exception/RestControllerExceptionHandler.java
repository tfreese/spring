// Created: 03.09.2018
package de.freese.spring.thymeleaf.exception;

import java.io.PrintWriter;
import java.io.StringWriter;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;
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
        HttpServletRequest httpServletRequest = getHttpServletRequest(request);

        return getException(httpServletRequest);
    }

    protected static HttpServletRequest getHttpServletRequest(final WebRequest request) {
        return (HttpServletRequest) ((ServletWebRequest) request).getNativeRequest();
    }

    protected static String getPath(final HttpServletRequest request) {
        return request.getRequestURL().toString();
    }

    protected static String getPath(final WebRequest request) {
        HttpServletRequest httpServletRequest = getHttpServletRequest(request);

        return getPath(httpServletRequest);
    }

    protected static HttpStatus getStatus(final HttpServletRequest request) {
        Integer statusCode = (Integer) request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);

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
        HttpServletRequest httpServletRequest = getHttpServletRequest(request);

        return getStatus(httpServletRequest);
    }

    public RestControllerExceptionHandler() {
        super();
    }

    protected ResponseEntity<Object> buildResponseEntity(final ApiError apiError, final WebRequest request, final HttpStatusCode statusCode, final Throwable ex, final String message) {
        this.logger.error(ex.getLocalizedMessage());

        // apiError.addDetail("detail_a", "value_a");
        // apiError.addDetail("detail_b", "value_b");

        apiError.setPath(getPath(request));
        apiError.setHttpStatus(statusCode.value());
        apiError.setMessage(message);
        apiError.setExceptionMessage(ex.getMessage());

        StringWriter sw = new StringWriter();

        try (PrintWriter pw = new PrintWriter(sw)) {
            ex.printStackTrace(pw);
        }

        apiError.setStackTrace(sw.toString());

        return new ResponseEntity<>(apiError, statusCode);
    }

    @ExceptionHandler(value = AccessDeniedException.class)
    protected ResponseEntity<Object> handleAccessDeniedException(final AccessDeniedException ex, final WebRequest request) {
        return buildResponseEntity(new ApiError(), request, HttpStatus.FORBIDDEN, ex, "Access Denied");
    }

    /**
     * Handles jakarta.validation.ConstraintViolationException. Thrown when @Validated fails.
     */
    @ExceptionHandler(ConstraintViolationException.class)
    protected ResponseEntity<Object> handleConstraintViolation(final ConstraintViolationException ex, final WebRequest request) {
        ApiError apiError = new ApiError();
        apiError.addValidationErrors(ex.getConstraintViolations());

        return buildResponseEntity(apiError, request, HttpStatus.BAD_REQUEST, ex, "Validation error");
    }

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(final Exception ex, final Object body, final HttpHeaders headers, final HttpStatusCode statusCode, final WebRequest request) {
        // return super.handleExceptionInternal(ex, body, headers, status, request);

        return buildResponseEntity(new ApiError(), request, statusCode, ex, null);
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
            responseEntity = buildResponseEntity(new ApiError(), request, HttpStatus.INTERNAL_SERVER_ERROR, ex, "Unhandled Exception: " + ex.getClass().getSimpleName());
        }

        return responseEntity;
    }

    @Override
    protected ResponseEntity<Object> handleHttpMediaTypeNotSupported(final HttpMediaTypeNotSupportedException ex, final HttpHeaders headers, final HttpStatusCode statusCode, final WebRequest request) {
        StringBuilder builder = new StringBuilder();
        builder.append(ex.getContentType());
        builder.append(" media type is not supported. Supported media types are ");
        ex.getSupportedMediaTypes().forEach(t -> builder.append(t).append(", "));

        return buildResponseEntity(new ApiError(), request, HttpStatus.UNSUPPORTED_MEDIA_TYPE, ex, builder.substring(0, builder.length() - 2));
    }

    // @ExceptionHandler(DataIntegrityViolationException.class)
    // protected ResponseEntity<Object> handleDataIntegrityViolation(DataIntegrityViolationException ex,
    // WebRequest request) {
    // if (ex.getCause() instanceof ConstraintViolationException) {
    // return buildResponseEntity(new ApiError(HttpStatus.CONFLICT, "Database error", ex.getCause()));
    // }
    //
    // return buildResponseEntity(new ApiError(HttpStatus.INTERNAL_SERVER_ERROR, ex));
    // }

    // @ExceptionHandler(jakarta.persistence.EntityNotFoundException.class)
    // protected ResponseEntity<Object> handleEntityNotFound(jakarta.persistence.EntityNotFoundException ex) {
    // return buildResponseEntity(new ApiError(HttpStatus.NOT_FOUND, ex));
    // }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(final HttpMessageNotReadableException ex, final HttpHeaders headers, final HttpStatusCode statusCode, final WebRequest request) {
        return buildResponseEntity(new ApiError(), request, HttpStatus.BAD_REQUEST, ex, "Malformed JSON request");
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotWritable(final HttpMessageNotWritableException ex, final HttpHeaders headers, final HttpStatusCode statusCode, final WebRequest request) {
        return buildResponseEntity(new ApiError(), request, HttpStatus.INTERNAL_SERVER_ERROR, ex, "Error writing JSON output");
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(final MethodArgumentNotValidException ex, final HttpHeaders headers, final HttpStatusCode statusCode, final WebRequest request) {
        ApiError apiError = new ApiError();
        apiError.addValidationErrors(ex.getBindingResult().getFieldErrors());
        apiError.addValidationError(ex.getBindingResult().getGlobalErrors());

        return buildResponseEntity(apiError, request, HttpStatus.BAD_REQUEST, ex, "Validation error");
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    protected ResponseEntity<Object> handleMethodArgumentTypeMismatch(final MethodArgumentTypeMismatchException ex, final WebRequest request) {
        String message = String.format("The parameter '%s' of value '%s' could not be converted to type '%s'", ex.getName(), ex.getValue(), ex.getRequiredType().getSimpleName());

        return buildResponseEntity(new ApiError(), request, HttpStatus.BAD_REQUEST, ex, message);
    }

    @Override
    protected ResponseEntity<Object> handleMissingServletRequestParameter(final MissingServletRequestParameterException ex, final HttpHeaders headers, final HttpStatusCode statusCode, final WebRequest request) {
        String error = ex.getParameterName() + " parameter is missing";

        return buildResponseEntity(new ApiError(), request, HttpStatus.BAD_REQUEST, ex, error);
    }

    @Override
    protected ResponseEntity<Object> handleNoHandlerFoundException(final NoHandlerFoundException ex, final HttpHeaders headers, final HttpStatusCode statusCode, final WebRequest request) {
        String error = String.format("Could not find the %s method for URL %s", ex.getHttpMethod(), ex.getRequestURL());

        return buildResponseEntity(new ApiError(), request, HttpStatus.BAD_REQUEST, ex, error);
    }
}
