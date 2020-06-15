/**
 * Created: 05.09.2018
 */

package de.freese.spring.thymeleaf.exception;

import javax.annotation.Resource;
import javax.validation.ConstraintViolationException;
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
public class ThymeleafControllerExceptionHandler
{
    /**
    *
    */
    @Value("${server.error.path:${error.path:/error}}")
    private String errorPage = null;

    /**
     *
     */
    @Resource
    private RestControllerExceptionHandler exceptionHandler = null;

    /**
     * Erstellt ein neues {@link ThymeleafControllerExceptionHandler} Object.
     */
    public ThymeleafControllerExceptionHandler()
    {
        super();
    }

    /**
     * @param model {@link Model}
     * @param ex {@link AccessDeniedException}
     * @param request {@link WebRequest}
     * @return String
     */
    @ExceptionHandler(value = AccessDeniedException.class)
    protected String handleAccessDeniedException(final Model model, final AccessDeniedException ex, final WebRequest request)
    {
        ResponseEntity<Object> responseEntity = this.exceptionHandler.handleAccessDeniedException(ex, request);

        model.addAttribute("apiError", responseEntity.getBody());

        return this.errorPage;
    }

    // /**
    // * Handle DataIntegrityViolationException, inspects the cause for different DB causes.
    // *
    // * @param model {@link Model}
    // * @param ex DataIntegrityViolationException
    // * @param request {@link WebRequest}
    // * @return String
    // */
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

    // /**
    // * Handle javax.persistence.EntityNotFoundException.
    // *
    // * @param model {@link Model}
    // * @param ex EntityNotFoundException
    // * @param request {@link WebRequest}
    // * @return String
    // */
    // @ExceptionHandler(javax.persistence.EntityNotFoundException.class)
    // protected String handleEntityNotFound(final Model model, final EntityNotFoundException ex, final WebRequest request)
    // {
    // ResponseEntity<Object> responseEntity = this.exceptionHandler.handleEntityNotFound(ex, request);
    //
    // model.addAttribute("apiError", responseEntity.getBody());
    //
    // return this.errorPage;
    // }

    /**
     * Handles javax.validation.ConstraintViolationException. Thrown when @Validated fails.
     *
     * @param model {@link Model}
     * @param ex {@link ConstraintViolationException}
     * @param request {@link WebRequest}
     * @return {@link ResponseEntity}
     */
    @ExceptionHandler(javax.validation.ConstraintViolationException.class)
    protected String handleConstraintViolation(final Model model, final javax.validation.ConstraintViolationException ex, final WebRequest request)
    {
        ResponseEntity<Object> responseEntity = this.exceptionHandler.handleConstraintViolation(ex, request);

        model.addAttribute("apiError", responseEntity.getBody());

        return this.errorPage;
    }

    /**
     * Allgemeiner ExceptionHandler.
     *
     * @param model {@link Model}
     * @param ex {@link Throwable}
     * @param request {@link WebRequest}
     * @return {@link ResponseEntity}
     */
    @ExceptionHandler(Throwable.class)
    protected String handleGenericException(final Model model, final Throwable ex, final WebRequest request)
    {
        ResponseEntity<Object> responseEntity = this.exceptionHandler.handleGenericException(ex, request);

        model.addAttribute("apiError", responseEntity.getBody());

        return this.errorPage;
    }

    /**
     * Handle Exception, handle generic Exception.class.
     *
     * @param model {@link Model}
     * @param ex {@link MethodArgumentTypeMismatchException}
     * @param request {@link WebRequest}
     * @return {@link ResponseEntity}
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    protected String handleMethodArgumentTypeMismatch(final Model model, final MethodArgumentTypeMismatchException ex, final WebRequest request)
    {
        ResponseEntity<Object> responseEntity = this.exceptionHandler.handleMethodArgumentTypeMismatch(ex, request);

        model.addAttribute("apiError", responseEntity.getBody());

        return this.errorPage;
    }
}
