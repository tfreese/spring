// Created: 01.12.2017
package de.freese.spring.hateoas.exception;

/**
 * @author Thomas Freese
 */
public class GreetingException extends RuntimeException
{
    /**
     *
     */
    private static final long serialVersionUID = 4807599092371462820L;

    /**
     * Erzeugt eine neue Instanz von {@link GreetingException}.
     *
     * @param message String
     */
    public GreetingException(final String message)
    {
        super(message);
    }
}
