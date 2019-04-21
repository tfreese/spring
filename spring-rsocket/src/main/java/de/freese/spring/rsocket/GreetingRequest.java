/**
 * Created: 21.04.2019
 */

package de.freese.spring.rsocket;

/**
 * @author Thomas Freese
 */
public class GreetingRequest
{
    /**
     *
     */
    private String name = null;

    /**
     * Erstellt ein neues {@link GreetingRequest} Object.
     */
    public GreetingRequest()
    {
        super();
    }

    /**
     * Erstellt ein neues {@link GreetingRequest} Object.
     *
     * @param name String
     */
    public GreetingRequest(final String name)
    {
        super();

        this.name = name;
    }

    /**
     * @return String
     */
    public String getName()
    {
        return this.name;
    }

    /**
     * @param name String
     */
    public void setName(final String name)
    {
        this.name = name;
    }
}