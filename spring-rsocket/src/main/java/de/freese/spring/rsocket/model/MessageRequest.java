// Created: 21.04.2019
package de.freese.spring.rsocket.model;

/**
 * @author Thomas Freese
 */
public class MessageRequest
{
    private String message;

    public MessageRequest()
    {
        super();
    }

    public MessageRequest(final String message)
    {
        super();

        this.message = message;
    }

    public String getMessage()
    {
        return this.message;
    }

    public void setMessage(final String message)
    {
        this.message = message;
    }

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append(getClass().getSimpleName());
        builder.append("[");
        builder.append("message=").append(this.message);
        builder.append("]");

        return builder.toString();
    }
}
