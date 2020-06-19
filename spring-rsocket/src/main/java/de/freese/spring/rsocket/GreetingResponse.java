/**
 * Created: 21.04.2019
 */

package de.freese.spring.rsocket;

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
    private long index = 0;

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
        this(greeting, 0L);
    }

    /**
     * Erstellt ein neues {@link GreetingResponse} Object.
     *
     * @param greeting String
     * @param index long
     */
    public GreetingResponse(final String greeting, final long index)
    {
        super();

        this.greeting = String.format("Hello %s", greeting);// @ %s", greeting, Instant.now());
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
    public long getIndex()
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
     * @param index long
     */
    public void setIndex(final long index)
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
        builder.append(", index=").append(this.index);
        builder.append("]");

        return builder.toString();
    }
}