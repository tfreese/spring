// Created: 29.01.24
package de.freese.spring.data.jpa.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * @author Thomas Freese
 */
@ControllerAdvice(annotations = RestController.class)
public class RestExceptionHandler extends ResponseEntityExceptionHandler {
    
    @ExceptionHandler(value = ObjectNotFoundException.class)
    protected ResponseEntity<Object> handleObjectNotFoundException(final ObjectNotFoundException ex, final WebRequest webRequest) {
        final HttpStatus httpStatus = HttpStatus.NOT_FOUND;

        final ProblemDetail problemDetail = ProblemDetail.forStatus(httpStatus);
        //        problemDetail.setType(exchange.getRequest().getURI());
        problemDetail.setTitle(ex.getMessage());
        problemDetail.setDetail(getDetail(ex).toString());

        return new ResponseEntity<>(problemDetail, httpStatus);
    }

    private StringBuilder getDetail(final Exception ex) {
        //        StringWriter sw = new StringWriter();
        //
        //        try (PrintWriter pw = new PrintWriter(sw, false)) {
        //            pw.println(ex);
        //            ex.printStackTrace(pw);
        //        }
        //
        //        return sw.toString();

        final StringBuilder sb = new StringBuilder();
        sb.append(ex);

        // Keep last 10 Elements.
        final StackTraceElement[] stackTraceOrigin = ex.getStackTrace();
        StackTraceElement[] stackTrace = stackTraceOrigin;

        if (stackTraceOrigin.length > 10) {
            stackTrace = new StackTraceElement[10];
            System.arraycopy(stackTraceOrigin, 0, stackTrace, 0, 10);
        }

        for (StackTraceElement stackTraceElement : stackTrace) {
            sb.append("\tat ").append(stackTraceElement);
        }

        if (stackTraceOrigin.length > 10) {
            sb.append("\t...");
        }

        return sb;
    }
}
