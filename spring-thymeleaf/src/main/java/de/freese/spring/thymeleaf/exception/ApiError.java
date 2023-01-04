// Created: 03.09.2018
package de.freese.spring.thymeleaf.exception;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import jakarta.validation.ConstraintViolation;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.jsontype.impl.TypeIdResolverBase;
import com.fasterxml.jackson.databind.type.TypeFactory;
import org.hibernate.validator.internal.engine.path.PathImpl;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;

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
    abstract static class AbstractApiSubError
    {
        private String subMessage;

        AbstractApiSubError()
        {
            super();
        }

        AbstractApiSubError(final String subMessage)
        {
            super();

            this.subMessage = subMessage;
        }

        public String getSubMessage()
        {
            return this.subMessage;
        }

        public void setSubMessage(final String subMessage)
        {
            this.subMessage = subMessage;
        }
    }

    /**
     * @author Thomas Freese
     */
    static class ApiValidationError extends AbstractApiSubError
    {
        private String field;

        private String object;

        private Object rejectedValue;

        ApiValidationError(final String object, final String message)
        {
            super(message);

            this.object = object;
        }

        ApiValidationError(final String object, final String field, final Object rejectedValue, final String message)
        {
            super(message);

            this.object = object;
            this.field = field;
            this.rejectedValue = rejectedValue;
        }

        public String getField()
        {
            return this.field;
        }

        public String getObject()
        {
            return this.object;
        }

        public Object getRejectedValue()
        {
            return this.rejectedValue;
        }

        public void setField(final String field)
        {
            this.field = field;
        }

        public void setObject(final String object)
        {
            this.object = object;
        }

        public void setRejectedValue(final Object rejectedValue)
        {
            this.rejectedValue = rejectedValue;
        }
    }

    // @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd hh:mm:ss.SSS")
    // @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSS")
    private final LocalDateTime timestamp;

    private Map<String, Serializable> details;

    private String exceptionMessage;

    private int httpStatus;

    private String message;

    private String path;

    private String stackTrace;

    private List<AbstractApiSubError> subErrors;

    public ApiError()
    {
        super();

        this.timestamp = LocalDateTime.now();
    }

    public void addDetail(final String key, final Serializable value)
    {
        if (this.details == null)
        {
            this.details = new TreeMap<>();
        }

        this.details.put(key, value);
    }

    public Map<String, Serializable> getDetails()
    {
        return this.details;
    }

    public String getExceptionMessage()
    {
        return this.exceptionMessage;
    }

    public int getHttpStatus()
    {
        return this.httpStatus;
    }

    public String getMessage()
    {
        return this.message;
    }

    public String getPath()
    {
        return this.path;
    }

    public String getStackTrace()
    {
        return this.stackTrace;
    }

    public List<AbstractApiSubError> getSubErrors()
    {
        return this.subErrors;
    }

    public LocalDateTime getTimestamp()
    {
        return this.timestamp;
    }

    public void setExceptionMessage(final String exceptionMessage)
    {
        this.exceptionMessage = exceptionMessage;
    }

    public void setHttpStatus(final int httpStatus)
    {
        this.httpStatus = httpStatus;
    }

    public void setMessage(final String message)
    {
        this.message = message;
    }

    public void setPath(final String path)
    {
        this.path = path;
    }

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

    void addValidationError(final List<ObjectError> globalErrors)
    {
        globalErrors.forEach(this::addValidationError);
    }

    void addValidationErrors(final List<FieldError> fieldErrors)
    {
        fieldErrors.forEach(this::addValidationError);
    }

    void addValidationErrors(final Set<ConstraintViolation<?>> constraintViolations)
    {
        constraintViolations.forEach(this::addValidationError);
    }

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

    private void addValidationError(final FieldError fieldError)
    {
        this.addValidationError(fieldError.getObjectName(), fieldError.getField(), fieldError.getRejectedValue(), fieldError.getDefaultMessage());
    }

    private void addValidationError(final ObjectError objectError)
    {
        this.addValidationError(objectError.getObjectName(), objectError.getDefaultMessage());
    }

    private void addValidationError(final String object, final String message)
    {
        addSubError(new ApiValidationError(object, message));
    }

    private void addValidationError(final String object, final String field, final Object rejectedValue, final String message)
    {
        addSubError(new ApiValidationError(object, field, rejectedValue, message));
    }
}

/**
 * @author Thomas Freese
 */
class LowerCaseClassNameResolver extends TypeIdResolverBase
{
    LowerCaseClassNameResolver(final JavaType baseType, final TypeFactory typeFactory)
    {
        super(baseType, typeFactory);
    }

    LowerCaseClassNameResolver()
    {
        super();
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
