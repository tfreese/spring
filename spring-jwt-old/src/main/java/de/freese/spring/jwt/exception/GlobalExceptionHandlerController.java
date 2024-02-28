// Created: 28.10.2018
package de.freese.spring.jwt.exception;

import java.io.IOException;
import java.util.Map;

import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.servlet.error.DefaultErrorAttributes;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * @author Thomas Freese
 */
@RestControllerAdvice
public class GlobalExceptionHandlerController extends ResponseEntityExceptionHandler {
    @Bean
    public ErrorAttributes errorAttributes() {
        return new DefaultErrorAttributes() {
            @Override
            public Map<String, Object> getErrorAttributes(final WebRequest webRequest, final ErrorAttributeOptions options) {
                final Map<String, Object> errorAttributes = super.getErrorAttributes(webRequest, options);

                // StackTrace entfernen.
                errorAttributes.remove("trace");

                return errorAttributes;
            }
        };
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Object> handleAccessDeniedException(final AccessDeniedException ex, final WebRequest request) {
        return handleExceptionInternal(ex, "Access denied", HttpStatus.FORBIDDEN, request);
        // return new ResponseEntity<>("Access denied", HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Object> handleAuthenticationException(final AuthenticationException ex, final WebRequest request) throws IOException {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleGenericException(final Exception ex) throws IOException {
        return new ResponseEntity<>("Something went wrong", HttpStatus.BAD_REQUEST);
    }

    protected ResponseEntity<Object> handleExceptionInternal(final Exception ex, final Object body, final HttpStatus status, final WebRequest request) {
        return super.handleExceptionInternal(ex, body, new HttpHeaders(), status, request);
    }
}
