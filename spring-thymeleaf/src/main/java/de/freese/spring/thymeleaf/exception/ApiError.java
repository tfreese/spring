/**
 * Created: 03.09.2018
 */
package de.freese.spring.thymeleaf.exception;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import javax.validation.ConstraintViolation;
import org.hibernate.validator.internal.engine.path.PathImpl;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.jsontype.impl.TypeIdResolverBase;
import com.fasterxml.jackson.databind.type.TypeFactory;

/**
 * @author Thomas Freese
 */
// @JsonTypeInfo(include = JsonTypeInfo.As.WRAPPER_OBJECT, use = JsonTypeInfo.Id.CUSTOM, property = "error", visible = true)
// @JsonTypeIdResolver(LowerCaseClassNameResolver.class)
public class ApiError
{
    /**
     * @author Thomas Freese
     */
    abstract class AbstractApiSubError
    {
        /**
         *
         */
        private String subMessage;

        /**
         * Erstellt ein neues {@link AbstractApiSubError} Object.
         */
        AbstractApiSubError()
        {
            super();
        }

        /**
         * Erstellt ein neues {@link AbstractApiSubError} Object.
         *
         * @param subMessage String
         */
        AbstractApiSubError(final String subMessage)
        {
            super();

            this.subMessage = subMessage;
        }

        /**
         * @return String
         */
        public String getSubMessage()
        {
            return this.subMessage;
        }

        /**
         * @param subMessage String
         */
        public void setSubMessage(final String subMessage)
        {
            this.subMessage = subMessage;
        }
    }

    /**
     * @author Thomas Freese
     */
    class ApiValidationError extends AbstractApiSubError
    {
        /**
         *
         */
        private String field;

        /**
         *
         */
        private String object;

        /**
         *
         */
        private Object rejectedValue;

        /**
         * Erstellt ein neues {@link ApiValidationError} Object.
         *
         * @param object String
         * @param message String
         */
        ApiValidationError(final String object, final String message)
        {
            super(message);

            this.object = object;
        }

        // (object, field, rejectedValue, message)

        /**
         * Erstellt ein neues {@link ApiValidationError} Object.
         *
         * @param object String
         * @param field String
         * @param rejectedValue Object
         * @param message String
         */
        ApiValidationError(final String object, final String field, final Object rejectedValue, final String message)
        {
            super(message);

            this.object = object;
            this.field = field;
            this.rejectedValue = rejectedValue;
        }

        /**
         * @return String
         */
        public String getField()
        {
            return this.field;
        }

        /**
         * @return String
         */
        public String getObject()
        {
            return this.object;
        }

        /**
         * @return Object
         */
        public Object getRejectedValue()
        {
            return this.rejectedValue;
        }

        /**
         * @param field String
         */
        public void setField(final String field)
        {
            this.field = field;
        }

        /**
         * @param object String
         */
        public void setObject(final String object)
        {
            this.object = object;
        }

        /**
         * @param rejectedValue Object
         */
        public void setRejectedValue(final Object rejectedValue)
        {
            this.rejectedValue = rejectedValue;
        }
    }

    /**
     *
     */
    private Map<String, Serializable> details;

    /**
     *
     */
    private String exceptionMessage;

    /**
    *
    */
    private int httpStatus;

    /**
    *
    */
    private String message;

    /**
    *
    */
    private String path;

    /**
     *
     */
    private String stackTrace;

    /**
     *
     */
    private List<AbstractApiSubError> subErrors;

    /**
    *
    */
    // @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd hh:mm:ss.SSS")
    // @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSS")
    private LocalDateTime timestamp;

    /**
     * Erstellt ein neues {@link ApiError} Object.
     */
    public ApiError()
    {
        super();

        this.timestamp = LocalDateTime.now();
    }

    /**
     * @param key String
     * @param value {@link Serializable}
     */
    public void addDetail(final String key, final Serializable value)
    {
        if (this.details == null)
        {
            this.details = new TreeMap<>();
        }

        this.details.put(key, value);
    }

    /**
     * @param subError {@link AbstractApiSubError}
     */
    private void addSubError(final AbstractApiSubError subError)
    {
        if (this.subErrors == null)
        {
            this.subErrors = new ArrayList<>();
        }

        this.subErrors.add(subError);
    }

    /**
     * Utility method for adding error of ConstraintViolation. Usually when a @Validated validation fails.
     *
     * @param cv the {@link ConstraintViolation}
     */
    private void addValidationError(final ConstraintViolation<?> cv)
    {
        // @formatter:off
        this.addValidationError(cv.getRootBeanClass().getSimpleName()
                , ((PathImpl) cv.getPropertyPath()).getLeafNode().asString()
                , cv.getInvalidValue()
                , cv.getMessage());
        // @formatter:on
    }

    /**
     * @param fieldError {@link FieldError}
     */
    private void addValidationError(final FieldError fieldError)
    {
        this.addValidationError(fieldError.getObjectName(), fieldError.getField(), fieldError.getRejectedValue(), fieldError.getDefaultMessage());
    }

    /**
     * @param globalErrors {@link List}
     */
    void addValidationError(final List<ObjectError> globalErrors)
    {
        globalErrors.forEach(this::addValidationError);
    }

    /**
     * @param objectError {@link ObjectError}
     */
    private void addValidationError(final ObjectError objectError)
    {
        this.addValidationError(objectError.getObjectName(), objectError.getDefaultMessage());
    }

    /**
     * @param object String
     * @param message String
     */
    private void addValidationError(final String object, final String message)
    {
        addSubError(new ApiValidationError(object, message));
    }

    /**
     * @param object String
     * @param field String
     * @param rejectedValue Object
     * @param message String
     */
    private void addValidationError(final String object, final String field, final Object rejectedValue, final String message)
    {
        addSubError(new ApiValidationError(object, field, rejectedValue, message));
    }

    /**
     * @param fieldErrors {@link List}
     */
    void addValidationErrors(final List<FieldError> fieldErrors)
    {
        fieldErrors.forEach(this::addValidationError);
    }

    /**
     * @param constraintViolations {@link Set}
     */
    void addValidationErrors(final Set<ConstraintViolation<?>> constraintViolations)
    {
        constraintViolations.forEach(this::addValidationError);
    }

    /**
     * @return {@link Map}<String,Serializable>
     */
    public Map<String, Serializable> getDetails()
    {
        return this.details;
    }

    /**
     * @return String
     */
    public String getExceptionMessage()
    {
        return this.exceptionMessage;
    }

    /**
     * @return int
     */
    public int getHttpStatus()
    {
        return this.httpStatus;
    }

    /**
     * @return String
     */
    public String getMessage()
    {
        return this.message;
    }

    /**
     * @return String
     */
    public String getPath()
    {
        return this.path;
    }

    /**
     * @return String
     */
    public String getStackTrace()
    {
        return this.stackTrace;
    }

    /**
     * @return {@link List}<ApiSubError>
     */
    public List<AbstractApiSubError> getSubErrors()
    {
        return this.subErrors;
    }

    /**
     * @return {@link LocalDateTime}
     */
    public LocalDateTime getTimestamp()
    {
        return this.timestamp;
    }

    /**
     * @param exceptionMessage String
     */
    public void setExceptionMessage(final String exceptionMessage)
    {
        this.exceptionMessage = exceptionMessage;
    }

    /**
     * @param httpStatus int
     */
    public void setHttpStatus(final int httpStatus)
    {
        this.httpStatus = httpStatus;
    }

    /**
     * @param message String
     */
    public void setMessage(final String message)
    {
        this.message = message;
    }

    /**
     * @param path String
     */
    public void setPath(final String path)
    {
        this.path = path;
    }

    /**
     * @param stackTrace String
     */
    public void setStackTrace(final String stackTrace)
    {
        this.stackTrace = stackTrace;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("ApiError [");
        builder.append("httpStatus=").append(this.httpStatus);
        builder.append(", path=").append(this.path);
        builder.append(", message=").append(this.message);
        builder.append(", exceptionMessage=").append(this.exceptionMessage);
        builder.append(", details=").append(this.details);
        builder.append(", timestamp=").append(this.timestamp);
        // builder.append(", stackTrace=").append(this.stackTrace);
        // builder.append(", subErrors=").append(this.subErrors);
        builder.append("]");

        return builder.toString();
    }
}

/**
 * @author Thomas Freese
 */
class LowerCaseClassNameResolver extends TypeIdResolverBase
{
    /**
     * Erstellt ein neues {@link LowerCaseClassNameResolver} Object.
     */
    public LowerCaseClassNameResolver()
    {
        super();
    }

    /**
     * Erstellt ein neues {@link LowerCaseClassNameResolver} Object.
     *
     * @param baseType {@link JavaType}
     * @param typeFactory {@link TypeFactory}
     */
    public LowerCaseClassNameResolver(final JavaType baseType, final TypeFactory typeFactory)
    {
        super(baseType, typeFactory);
    }

    /**
     * @see com.fasterxml.jackson.databind.jsontype.TypeIdResolver#getMechanism()
     */
    @Override
    public JsonTypeInfo.Id getMechanism()
    {
        return JsonTypeInfo.Id.CUSTOM;
    }

    /**
     * @see com.fasterxml.jackson.databind.jsontype.TypeIdResolver#idFromValue(java.lang.Object)
     */
    @Override
    public String idFromValue(final Object value)
    {
        return value.getClass().getSimpleName().toLowerCase();
    }

    /**
     * @see com.fasterxml.jackson.databind.jsontype.TypeIdResolver#idFromValueAndType(java.lang.Object, java.lang.Class)
     */
    @Override
    public String idFromValueAndType(final Object value, final Class<?> suggestedType)
    {
        return idFromValue(value);
    }
}
