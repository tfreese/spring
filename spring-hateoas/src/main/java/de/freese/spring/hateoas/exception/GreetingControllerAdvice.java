// Created: 01.12.2017
package de.freese.spring.hateoas.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import de.freese.spring.hateoas.GreetingController;

/**
 * @author Thomas Freese
 */
@RestControllerAdvice(assignableTypes = GreetingController.class)
public class GreetingControllerAdvice {
    // @ExceptionHandler(GreetingException.class)
    // // @ResponseStatus(HttpStatus.NOT_IMPLEMENTED)
    // @ResponseStatus(HttpStatus.BAD_REQUEST)
    // public VndErrors greetingExceptionHandler(final GreetingException ex) {
    // final StringWriter sw = new StringWriter();
    //
    // try (PrintWriter pw = new PrintWriter(sw)) {
    // ex.printStackTrace(pw);
    // }
    //
    // final String stackTrace = sw.toString();
    //
    // return new VndErrors(ex.getMessage(), stackTrace);
    // }

    // @ExceptionHandler(GreetingException.class)
    // // @ResponseStatus(HttpStatus.NOT_IMPLEMENTED)
    // @ResponseStatus(HttpStatus.BAD_REQUEST)
    // public GreetingException greetingExceptionHandler(final GreetingException ex) {
    // return ex;
    // }

    @ExceptionHandler
    public ResponseEntity<Exception> defaultHandler(final Exception ex) {
        return ResponseEntity.badRequest().body(ex);
    }

    @ExceptionHandler(GreetingException.class)
    public ResponseEntity<Exception> greetingExceptionHandler(final GreetingException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex);
        // return ResponseEntity.status(999).body(ex);
    }
}
