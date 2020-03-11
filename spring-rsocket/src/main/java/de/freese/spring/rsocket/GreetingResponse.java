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
    *
    */
    private Long index = null;

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
        this(greeting, null);
    }

    /**
     * Erstellt ein neues {@link GreetingResponse} Object.
     *
     * @param greeting String
     * @param index Long
     */
    public GreetingResponse(final String greeting, final Long index)
    {
        super();

        this.greeting = String.format("Hello %s @ %s", greeting, Instant.now());
        this.index = index;
    }

    /**
     * @return String
     */
    public String getGreeting()
    {
        return this.greeting;
    }

    /**
     * @return Long
     */
    public Long getIndex()
    {
        return this.index;
    }

    /**
     * @param greeting String
     */
    public void setGreeting(final String greeting)
    {
        this.greeting = greeting;
    }

    /**
     * @param index Long
     */
    public void setIndex(final Long index)
    {
        this.index = index;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("GreetingResponse [");
        builder.append("greeting=").append(this.greeting);

        if (this.index != null)
        {
            builder.append(", index=").append(this.index);
        }

        builder.append("]");

        return builder.toString();
    }
}