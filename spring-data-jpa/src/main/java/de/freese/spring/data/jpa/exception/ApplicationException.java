// Created: 29.01.24
package de.freese.spring.data.jpa.exception;

import java.io.Serial;

/**
 * @author Thomas Freese
 */
public class ApplicationException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = -6441373594183229264L;

    public ApplicationException() {
        super();
    }

    public ApplicationException(final String message) {
        super(message);
    }

    public ApplicationException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
