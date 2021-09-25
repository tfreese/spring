// Created: 21.04.2019
package de.freese.spring.rsocket.client.data;

/**
 * @author Thomas Freese
 */
public class MessageResponse
{
    /**
    *
    */
    private long index;
    /**
     *
     */
    private String message;

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
