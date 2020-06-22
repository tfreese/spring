/**
 * Created: 28.10.2018
 */

package de.freese.spring.jwt.exception;

import java.io.IOException;
import java.util.Map;
import javax.servlet.http.HttpServletResponse;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.servlet.error.DefaultErrorAttributes;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
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
public class GlobalExceptionHandlerController extends ResponseEntityExceptionHandler
{
    /**
     * Erstellt ein neues {@link GlobalExceptionHandlerController} Object.
     */
    public GlobalExceptionHandlerController()
    {
        super();
    }

    /**
     * @return {@link ErrorAttributes}
     */
    @Bean
    public ErrorAttributes errorAttributes()
    {
        return new DefaultErrorAttributes()
        {
            /**
             * @see org.springframework.boot.web.servlet.error.DefaultErrorAttributes#getErrorAttributes(org.springframework.web.context.request.WebRequest,
             *      org.springframework.boot.web.error.ErrorAttributeOptions)
             */
            @Override
            public Map<String, Object> getErrorAttributes(final WebRequest webRequest, final ErrorAttributeOptions options)
            {
                Map<String, Object> errorAttributes = super.getErrorAttributes(webRequest, options);

                // StackTrace verbergen.
                errorAttributes.remove("exception");

                return errorAttributes;
            }
        };
    }

    /**
     * @param res {@link HttpServletResponse}
     * @throws IOException Falls was schief geht.
     */
    @ExceptionHandler(AccessDeniedException.class)
    public void handleAccessDeniedException(final HttpServletResponse res) throws IOException
    {
        res.sendError(HttpStatus.FORBIDDEN.value(), "Access denied");
    }

    /**
     * @param res {@link HttpServletResponse}
     * @param ex {@link AuthenticationException}
     * @throws IOException Falls was schief geht.
     */
    @ExceptionHandler(AuthenticationException.class)
    public void handleCustomException(final HttpServletResponse res, final AuthenticationException ex) throws IOException
    {
        res.sendError(HttpStatus.FORBIDDEN.value(), ex.getMessage());
    }

    /**
     * @param res {@link HttpServletResponse}
     * @throws IOException Falls was schief geht.
     */
    @ExceptionHandler(Exception.class)
    public void handleException(final HttpServletResponse res) throws IOException
    {
        res.sendError(HttpStatus.BAD_REQUEST.value(), "Something went wrong");
    }
}
