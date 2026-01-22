// Created: 29.01.24
package de.freese.spring.data.jpa.exception;

import java.net.URI;
import java.util.List;
import java.util.stream.Stream;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;

import org.jspecify.annotations.Nullable;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import org.springframework.web.util.WebUtils;
import tools.jackson.databind.json.JsonMapper;

/**
 * @author Thomas Freese
 */
@ControllerAdvice
// @ControllerAdvice(annotations = RestController.class)
// @RestControllerAdvice
public class RestExceptionHandler extends ResponseEntityExceptionHandler {

    public static ProblemDetail createProblemDetail(final Exception exception, final HttpStatusCode httpStatusCode, final JsonMapper jsonMapper) {
        final ProblemDetail problemDetail = ProblemDetail.forStatus(httpStatusCode);
        problemDetail.setTitle(exception.getClass().getName() + ": " + exception.getMessage());
        problemDetail.setDetail(stackTraceToJson(jsonMapper, exception));

        return problemDetail;
    }

    private static String stackTraceToJson(final JsonMapper jsonMapper, final Exception exception) {
        final List<StackTraceElement> stackTraceList = Stream.of(exception.getStackTrace()).limit(6).toList();

        return jsonMapper.writeValueAsString(stackTraceList);
    }

    @Resource
    private JsonMapper jsonMapper;

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(final Exception exception, @Nullable Object body, final HttpHeaders headers,
                                                             final HttpStatusCode statusCode, final WebRequest request) {
        if (request instanceof ServletWebRequest servletWebRequest) {
            final HttpServletResponse response = servletWebRequest.getResponse();

            if (response != null && response.isCommitted()) {
                if (logger.isWarnEnabled()) {
                    logger.warn("Response already committed. Ignoring: " + exception);
                }

                return null;
            }
        }

        if (body == null && exception instanceof ErrorResponse errorResponse) {
            body = errorResponse.updateAndGetBody(getMessageSource(), LocaleContextHolder.getLocale());
        }

        if (statusCode.equals(HttpStatus.INTERNAL_SERVER_ERROR) && body == null) {
            request.setAttribute(WebUtils.ERROR_EXCEPTION_ATTRIBUTE, exception, RequestAttributes.SCOPE_REQUEST);
        }

        // Start Additional Code.
        if (body == null) {
            final ProblemDetail problemDetail = createProblemDetail(exception, statusCode, jsonMapper);

            if (request instanceof final ServletWebRequest servletWebRequest) {
                problemDetail.setInstance(URI.create(servletWebRequest.getRequest().getRequestURI()));
            }
            else {
                problemDetail.setInstance(URI.create(request.getContextPath()));
            }

            body = problemDetail;
        }
        // End Additional Code.

        return createResponseEntity(body, headers, statusCode, request);
    }

    @ExceptionHandler(value = ObjectNotFoundException.class)
    protected ResponseEntity<Object> handleObjectNotFoundException(final ObjectNotFoundException exception, final WebRequest webRequest) {
        return handleExceptionInternal(exception, null, null, HttpStatus.NOT_FOUND, webRequest);
    }

    @ExceptionHandler(value = RuntimeException.class)
    protected ResponseEntity<Object> handleRuntimeException(final RuntimeException exception, final WebRequest webRequest) {
        return handleExceptionInternal(exception, null, null, HttpStatus.INTERNAL_SERVER_ERROR, webRequest);
    }
}
