/**
 * Created: 21.04.2019
 */

package de.freese.spring.rsocket;

import java.time.Instant;

/**
 * @author Thomas Freese
 */
public class GreetingResponse
{
    /**
     *
     */
    private String greeting = null;

    /**
     * Erstellt ein neues {@link GreetingResponse} Object.
     */
    public GreetingResponse()
    {
        super();
    }

    /**
     * Erstellt ein neues {@link GreetingResponse} Object.
     *
     * @param greeting String
     */
    public GreetingResponse(final String greeting)
    {
        super();

        this.greeting = "Hello " + greeting + " @ " + Instant.now();
    }

    /**
     * @return String
     */
    public String getGreeting()
    {
        return this.greeting;
    }

    /**
     * @param greeting String
     */
    public void setGreeting(final String greeting)
    {
        this.greeting = greeting;
    }
}