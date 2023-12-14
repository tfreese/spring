// Created: 05.09.2018
package de.freese.spring.thymeleaf.exception;

import jakarta.annotation.Resource;
import jakarta.validation.ConstraintViolationException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import de.freese.spring.thymeleaf.ThymeleafController;

/**
 * @author Thomas Freese
 */
@ControllerAdvice(annotations = ThymeleafController.class)
public class ThymeleafControllerExceptionHandler {
    @Value("${server.error.path:${error.path:/error}}")
    private String errorPage;

    @Resource
    private RestControllerExceptionHandler exceptionHandler;

    @ExceptionHandler(value = AccessDeniedException.class)
    protected String handleAccessDeniedException(final Model model, final AccessDeniedException ex, final WebRequest request) {
        final ResponseEntity<Object> responseEntity = this.exceptionHandler.handleAccessDeniedException(ex, request);

        model.addAttribute("apiError", responseEntity.getBody());

        return this.errorPage;
    }

    // @ExceptionHandler(DataIntegrityViolationException.class)
    // protected String handleDataIntegrityViolation(final Model model, final DataIntegrityViolationException ex, final WebRequest request)
    // {
    // ResponseEntity<Object> responseEntity = null;
    //
    // if (ex.getCause() instanceof ConstraintViolationException)
    // {
    // responseEntity = this.exceptionHandler.handleDataIntegrityViolation(ex, request);
    // }
    // else
    // {
    // responseEntity = this.exceptionHandler.handleGenericException(ex, request);
    // }
    //
    // model.addAttribute("apiError", responseEntity.getBody());
    //
    // return this.errorPage;
    // }

    // @ExceptionHandler(jakarta.persistence.EntityNotFoundException.class)
    // protected String handleEntityNotFound(final Model model, final EntityNotFoundException ex, final WebRequest request)
    // {
    // final ResponseEntity<Object> responseEntity = this.exceptionHandler.handleEntityNotFound(ex, request);
    //
    // model.addAttribute("apiError", responseEntity.getBody());
    //
    // return this.errorPage;
    // }

    @ExceptionHandler(ConstraintViolationException.class)
    protected String handleConstraintViolation(final Model model, final ConstraintViolationException ex, final WebRequest request) {
        final ResponseEntity<Object> responseEntity = this.exceptionHandler.handleConstraintViolation(ex, request);

        model.addAttribute("apiError", responseEntity.getBody());

        return this.errorPage;
    }

    @ExceptionHandler(Throwable.class)
    protected String handleGenericException(final Model model, final Throwable ex, final WebRequest request) {
        final ResponseEntity<Object> responseEntity = this.exceptionHandler.handleGenericException(ex, request);

        model.addAttribute("apiError", responseEntity.getBody());

        return this.errorPage;
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    protected String handleMethodArgumentTypeMismatch(final Model model, final MethodArgumentTypeMismatchException ex, final WebRequest request) {
        final ResponseEntity<Object> responseEntity = this.exceptionHandler.handleMethodArgumentTypeMismatch(ex, request);

        model.addAttribute("apiError", responseEntity.getBody());

        return this.errorPage;
    }
}
