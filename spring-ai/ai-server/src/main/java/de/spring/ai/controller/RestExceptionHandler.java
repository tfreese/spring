package de.spring.ai.controller;

import java.net.URI;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

import jakarta.annotation.Resource;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import tools.jackson.databind.json.JsonMapper;

/**
 * @author Thomas Freese
 */
@ControllerAdvice
public class RestExceptionHandler extends ResponseEntityExceptionHandler {

    public static ProblemDetail createProblemDetail(final Exception exception, final HttpStatusCode httpStatusCodes, final JsonMapper jsonMapper) {
        final ProblemDetail problemDetail = ProblemDetail.forStatus(httpStatusCodes);
        problemDetail.setTitle(exception.getMessage());
        problemDetail.setProperty("throwableName", exception.getClass().getName());
        problemDetail.setDetail(stackTraceToJson(jsonMapper, exception));

        return problemDetail;
    }

    private static String stackTraceToJson(final JsonMapper jsonMapper, final Exception exception) {
        final List<StackTraceElement> stackTraceList = Stream.of(exception.getStackTrace()).limit(6).toList();

        return jsonMapper.writeValueAsString(stackTraceList);

        // return """
        //         {
        //         "className": %s,
        //         "methodName": "stackTraceToJson",
        //         "lineNumber": 39
        //         }
        //         """.formatted(RestExceptionHandler.class.getName());
    }

    @Resource
    private JsonMapper jsonMapper;

    @ExceptionHandler(RuntimeException.class)
    protected ResponseEntity<Object> handleRuntimeException(final RuntimeException exception, final WebRequest webRequest) {
        logger.error(exception.getMessage(), exception);

        return handleSystemExceptions(exception, HttpStatus.INTERNAL_SERVER_ERROR, webRequest);
    }

    private ResponseEntity<Object> handleSystemExceptions(final Exception exception,
                                                          final HttpStatusCode statusCode,
                                                          final WebRequest webRequest) {
        return handleSystemExceptions(exception, statusCode, webRequest, problemDetail -> {
        });
    }

    private ResponseEntity<Object> handleSystemExceptions(final Exception exception,
                                                          final HttpStatusCode statusCode,
                                                          final WebRequest webRequest,
                                                          final Consumer<ProblemDetail> problemDetailConfigurer) {
        final ProblemDetail problemDetail = createProblemDetail(exception, statusCode, jsonMapper);

        if (webRequest instanceof final ServletWebRequest servletWebRequest) {
            problemDetail.setInstance(URI.create(servletWebRequest.getRequest().getRequestURI()));
        } else {
            problemDetail.setInstance(URI.create(webRequest.getContextPath()));
        }

        problemDetailConfigurer.accept(problemDetail);

        return handleExceptionInternal(exception, problemDetail, new HttpHeaders(), statusCode, webRequest);
    }
}
