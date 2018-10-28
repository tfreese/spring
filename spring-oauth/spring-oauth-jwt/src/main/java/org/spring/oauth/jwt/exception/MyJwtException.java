/**
 * Created: 28.10.2018
 */

package org.spring.oauth.jwt.exception;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;

/**
 * @author Thomas Freese
 */
public class MyJwtException extends AuthenticationException
{
    /**
     *
     */
    private static final long serialVersionUID = 769907869039990697L;

    /**
     *
     */
    private final HttpStatus httpStatus;

    /**
     * Erstellt ein neues {@link MyJwtException} Object.
     *
     * @param message String
     * @param httpStatus HttpStatus
     */
    public MyJwtException(final String message, final HttpStatus httpStatus)
    {
        super(message);

        this.httpStatus = httpStatus;
    }

    /**
     * @return {@link HttpStatus}
     */
    public HttpStatus getHttpStatus()
    {
        return this.httpStatus;
    }
}
