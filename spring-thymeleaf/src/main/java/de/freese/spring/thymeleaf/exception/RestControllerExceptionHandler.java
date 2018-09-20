/**
 * Created: 03.09.2018
 */

package de.freese.spring.thymeleaf.exception;

import java.io.PrintWriter;
import java.io.StringWriter;
import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
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
public class RestControllerExceptionHandler extends ResponseEntityExceptionHandler
{
    /**
     * @param request {@link HttpServletRequest}
     * @return {@link Exception}
     */
    protected static Exception getException(final HttpServletRequest request)
    {
        Exception ex = (Exception) request.getAttribute(RequestDispatcher.ERROR_EXCEPTION);

        return ex;
    }

    /**
     * @param request {@link WebRequest}
     * @return {@link Exception}
     */
    protected static Exception getException(final WebRequest request)
    {
        HttpServletRequest httpServletRequest = getHttpServletRequest(request);
        Exception ex = getException(httpServletRequest);

        return ex;
    }

    /**
     * @param request {@link WebRequest}
     * @return {@link HttpServletRequest}
     */
    protected static HttpServletRequest getHttpServletRequest(final WebRequest request)
    {
        HttpServletRequest httpServletRequest = (HttpServletRequest) ((ServletWebRequest) request).getNativeRequest();

        return httpServletRequest;
    }

    /**
     * Liefert den URL-Pfad des Requests.
     *
     * @param request {@link HttpServletRequest}
     * @return String
     */
    protected static String getPath(final HttpServletRequest request)
    {
        String path = request.getRequestURL().toString();

        return path;
    }

    /**
     * Liefert den URL-Pfad des Requests.
     *
     * @param request {@link WebRequest}
     * @return String
     */
    protected static String getPath(final WebRequest request)
    {
        HttpServletRequest httpServletRequest = getHttpServletRequest(request);
        String path = getPath(httpServletRequest);

        return path;
    }

    /**
     * @param request {@link HttpServletRequest}
     * @return {@link HttpStatus}
     */
    protected static HttpStatus getStatus(final HttpServletRequest request)
    {
        Integer statusCode = (Integer) request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);

        if (statusCode == null)
        {
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }

        try
        {
            return HttpStatus.valueOf(statusCode);
        }
        catch (Exception ex)
        {
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }
    }

    /**
     * @param request {@link WebRequest}
     * @return {@link HttpStatus}
     */
    protected static HttpStatus getStatus(final WebRequest request)
    {
        HttpServletRequest httpServletRequest = getHttpServletRequest(request);
        HttpStatus httpStatus = getStatus(httpServletRequest);

        return httpStatus;
    }

    /**
     * Erstellt ein neues {@link RestControllerExceptionHandler} Object.
     */
    public RestControllerExceptionHandler()
    {
        super();
    }

    /**
     * @param apiError {@link ApiError}
     * @param request {@link WebRequest}
     * @param httpStatus {@link HttpStatus}
     * @param ex {@link Throwable}
     * @param message String
     * @return {@link ResponseEntity}
     */
    protected ResponseEntity<Object> buildResponseEntity(final ApiError apiError, final WebRequest request, final HttpStatus httpStatus, final Throwable ex,
                                                         final String message)
    {
        this.logger.error(ex.getLocalizedMessage());

        // apiError.addDetail("detail_a", "value_a");
        // apiError.addDetail("detail_b", "value_b");

        apiError.setPath(getPath(request));
        apiError.setHttpStatus(httpStatus.value());
        apiError.setMessage(message);
        apiError.setExceptionMessage(ex.getMessage());

        StringWriter sw = new StringWriter();

        try (PrintWriter pw = new PrintWriter(sw))
        {
            ex.printStackTrace(pw);
        }

        apiError.setStackTrace(sw.toString());

        return new ResponseEntity<>(apiError, httpStatus);
    }

    /**
     * @param ex {@link AccessDeniedException}
     * @param request {@link WebRequest}
     * @return {@link ResponseEntity}
     */
    @ExceptionHandler(value = AccessDeniedException.class)
    protected ResponseEntity<Object> handleAccessDeniedException(final AccessDeniedException ex, final WebRequest request)
    {
        ResponseEntity<Object> responseEntity = buildResponseEntity(new ApiError(), request, HttpStatus.FORBIDDEN, ex, "Access Denied");

        return responseEntity;
    }

    /**
     * Handles javax.validation.ConstraintViolationException. Thrown when @Validated fails.
     *
     * @param ex {@link ConstraintViolationException}
     * @param request {@link WebRequest}
     * @return {@link ResponseEntity}
     */
    @ExceptionHandler(javax.validation.ConstraintViolationException.class)
    protected ResponseEntity<Object> handleConstraintViolation(final javax.validation.ConstraintViolationException ex, final WebRequest request)
    {
        ApiError apiError = new ApiError();
        apiError.addValidationErrors(ex.getConstraintViolations());

        return buildResponseEntity(apiError, request, HttpStatus.BAD_REQUEST, ex, "Validation error");
    }

    /**
     * @see org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler#handleExceptionInternal(java.lang.Exception, java.lang.Object,
     *      org.springframework.http.HttpHeaders, org.springframework.http.HttpStatus, org.springframework.web.context.request.WebRequest)
     */
    @Override
    protected ResponseEntity<Object> handleExceptionInternal(final Exception ex, final Object body, final HttpHeaders headers, final HttpStatus status,
                                                             final WebRequest request)
    {
        // return super.handleExceptionInternal(ex, body, headers, status, request);

        return buildResponseEntity(new ApiError(), request, status, ex, null);
    }

    /**
     * Allgemeiner ExceptionHandler.
     *
     * @param ex {@link Throwable}
     * @param request {@link WebRequest}
     * @return {@link ResponseEntity}
     */
    @ExceptionHandler(Throwable.class)
    protected ResponseEntity<Object> handleGenericException(final Throwable ex, final WebRequest request)
    {
        ResponseEntity<Object> responseEntity = null;

        try
        {
            if (ex instanceof Exception)
            {
                responseEntity = handleException((Exception) ex, request);
            }
        }
        catch (Exception ex2)
        {
        }

        if (responseEntity == null)
        {
            responseEntity =
                    buildResponseEntity(new ApiError(), request, HttpStatus.INTERNAL_SERVER_ERROR, ex, "Unhandled Exception: " + ex.getClass().getSimpleName());
        }

        return responseEntity;
    }

    /**
     * @see org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler#handleHttpMediaTypeNotSupported(org.springframework.web.HttpMediaTypeNotSupportedException,
     *      org.springframework.http.HttpHeaders, org.springframework.http.HttpStatus, org.springframework.web.context.request.WebRequest)
     */
    @Override
    protected ResponseEntity<Object> handleHttpMediaTypeNotSupported(final HttpMediaTypeNotSupportedException ex, final HttpHeaders headers,
                                                                     final HttpStatus status, final WebRequest request)
    {
        StringBuilder builder = new StringBuilder();
        builder.append(ex.getContentType());
        builder.append(" media type is not supported. Supported media types are ");
        ex.getSupportedMediaTypes().forEach(t -> builder.append(t).append(", "));

        return buildResponseEntity(new ApiError(), request, HttpStatus.UNSUPPORTED_MEDIA_TYPE, ex, builder.substring(0, builder.length() - 2));
    }

    // TODO
    // /**
    // * Handle DataIntegrityViolationException, inspects the cause for different DB causes.
    // *
    // * @param ex the DataIntegrityViolationException
    // * @return the ApiError object
    // */
    // @ExceptionHandler(DataIntegrityViolationException.class)
    // protected ResponseEntity<Object> handleDataIntegrityViolation(DataIntegrityViolationException ex,
    // WebRequest request) {
    // if (ex.getCause() instanceof ConstraintViolationException) {
    // return buildResponseEntity(new ApiError(HttpStatus.CONFLICT, "Database error", ex.getCause()));
    // }
    //
    // return buildResponseEntity(new ApiError(HttpStatus.INTERNAL_SERVER_ERROR, ex));
    // }

    // TODO
    // /**
    // * Handle javax.persistence.EntityNotFoundException
    // */
    // @ExceptionHandler(javax.persistence.EntityNotFoundException.class)
    // protected ResponseEntity<Object> handleEntityNotFound(javax.persistence.EntityNotFoundException ex) {
    // return buildResponseEntity(new ApiError(HttpStatus.NOT_FOUND, ex));
    // }

    /**
     * @see org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler#handleHttpMessageNotReadable(org.springframework.http.converter.HttpMessageNotReadableException,
     *      org.springframework.http.HttpHeaders, org.springframework.http.HttpStatus, org.springframework.web.context.request.WebRequest)
     */
    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(final HttpMessageNotReadableException ex, final HttpHeaders headers, final HttpStatus status,
                                                                  final WebRequest request)
    {
        return buildResponseEntity(new ApiError(), request, HttpStatus.BAD_REQUEST, ex, "Malformed JSON request");
    }

    /**
     * @see org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler#handleHttpMessageNotWritable(org.springframework.http.converter.HttpMessageNotWritableException,
     *      org.springframework.http.HttpHeaders, org.springframework.http.HttpStatus, org.springframework.web.context.request.WebRequest)
     */
    @Override
    protected ResponseEntity<Object> handleHttpMessageNotWritable(final HttpMessageNotWritableException ex, final HttpHeaders headers, final HttpStatus status,
                                                                  final WebRequest request)
    {
        return buildResponseEntity(new ApiError(), request, HttpStatus.INTERNAL_SERVER_ERROR, ex, "Error writing JSON output");
    }

    /**
     * @see org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler#handleMethodArgumentNotValid(org.springframework.web.bind.MethodArgumentNotValidException,
     *      org.springframework.http.HttpHeaders, org.springframework.http.HttpStatus, org.springframework.web.context.request.WebRequest)
     */
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(final MethodArgumentNotValidException ex, final HttpHeaders headers, final HttpStatus status,
                                                                  final WebRequest request)
    {
        ApiError apiError = new ApiError();
        apiError.addValidationErrors(ex.getBindingResult().getFieldErrors());
        apiError.addValidationError(ex.getBindingResult().getGlobalErrors());

        return buildResponseEntity(apiError, request, HttpStatus.BAD_REQUEST, ex, "Validation error");
    }

    /**
     * Handle Exception, handle generic Exception.class.
     *
     * @param ex {@link MethodArgumentTypeMismatchException}
     * @param request {@link WebRequest}
     * @return {@link ResponseEntity}
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    protected ResponseEntity<Object> handleMethodArgumentTypeMismatch(final MethodArgumentTypeMismatchException ex, final WebRequest request)
    {
        String message = String.format("The parameter '%s' of value '%s' could not be converted to type '%s'", ex.getName(), ex.getValue(),
                ex.getRequiredType().getSimpleName());

        return buildResponseEntity(new ApiError(), request, HttpStatus.BAD_REQUEST, ex, message);
    }

    /**
     * @see org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler#handleMissingServletRequestParameter(org.springframework.web.bind.MissingServletRequestParameterException,
     *      org.springframework.http.HttpHeaders, org.springframework.http.HttpStatus, org.springframework.web.context.request.WebRequest)
     */
    @Override
    protected ResponseEntity<Object> handleMissingServletRequestParameter(final MissingServletRequestParameterException ex, final HttpHeaders headers,
                                                                          final HttpStatus status, final WebRequest request)
    {
        String error = ex.getParameterName() + " parameter is missing";

        return buildResponseEntity(new ApiError(), request, HttpStatus.BAD_REQUEST, ex, error);
    }

    /**
     * @see org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler#handleNoHandlerFoundException(org.springframework.web.servlet.NoHandlerFoundException,
     *      org.springframework.http.HttpHeaders, org.springframework.http.HttpStatus, org.springframework.web.context.request.WebRequest)
     */
    @Override
    protected ResponseEntity<Object> handleNoHandlerFoundException(final NoHandlerFoundException ex, final HttpHeaders headers, final HttpStatus status,
                                                                   final WebRequest request)
    {
        String error = String.format("Could not find the %s method for URL %s", ex.getHttpMethod(), ex.getRequestURL());

        return buildResponseEntity(new ApiError(), request, HttpStatus.BAD_REQUEST, ex, error);
    }
}
