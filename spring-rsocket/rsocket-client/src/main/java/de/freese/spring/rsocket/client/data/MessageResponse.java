/**
 * Created: 21.04.2019
 */

package de.freese.spring.rsocket.client.data;

/**
 * @author Thomas Freese
 */
public class MessageResponse
{
    /**
    *
    */
    private long index = 0;

    /**
     *
     */
    private String message = null;

    /**
     * Erstellt ein neues {@link MessageResponse} Object.
     */
    public MessageResponse()
    {
        super();
    }

    // /**
    // * Erstellt ein neues {@link MessageResponse} Object.
    // *
    // * @param message String
    // */
    // public MessageResponse(final String message)
    // {
    // this(message, 0L);
    // }
    //
    // /**
    // * Erstellt ein neues {@link MessageResponse} Object.
    // *
    // * @param message String
    // * @param index long
    // */
    // public MessageResponse(final String message, final long index)
    // {
    // super();
    //
    // this.message = String.format("Hello %s", message);// @ %s", greeting, Instant.now());
    // this.index = index;
    // }

    /**
     * @return Long
     */
    public long getIndex()
    {
        return this.index;
    }

    /**
     * @return String
     */
    public String getMessage()
    {
        return this.message;
    }

    /**
     * @param index long
     */
    public void setIndex(final long index)
    {
        this.index = index;
    }

    /**
     * @param message String
     */
    public void setMessage(final String message)
    {
        this.message = message;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("[");
        builder.append("message=").append(this.message);
        builder.append(", index=").append(this.index);
        builder.append("]");

        return builder.toString();
    }
}