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
    private Long index = null;

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
        this(name, null);
    }

    /**
     * Erstellt ein neues {@link GreetingRequest} Object.
     *
     * @param name String
     * @param index Long
     */
    public GreetingRequest(final String name, final Long index)
    {
        super();

        this.name = name;
        this.index = index;
    }

    /**
     * @return Long
     */
    public Long getIndex()
    {
        return this.index;
    }

    /**
     * @return String
     */
    public String getName()
    {
        return this.name;
    }

    /**
     * @param index Long
     */
    public void setIndex(final Long index)
    {
        this.index = index;
    }

    /**
     * @param name String
     */
    public void setName(final String name)
    {
        this.name = name;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("GreetingRequest [");
        builder.append("name=").append(this.name);

        if (this.index != null)
        {
            builder.append(", index=").append(this.index);
        }

        builder.append("]");

        return builder.toString();
    }
}