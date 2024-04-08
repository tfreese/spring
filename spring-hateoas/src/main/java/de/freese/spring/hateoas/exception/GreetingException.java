// Created: 01.12.2017
package de.freese.spring.hateoas.exception;

import java.io.Serial;

/**
 * @author Thomas Freese
 */
public class GreetingException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = 4807599092371462820L;

    public GreetingException(final String message) {
        super(message);
    }
}
