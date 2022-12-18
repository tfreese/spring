// Created: 31.01.2019
package de.freese.spring.messaging.amqp.qpid;

/**
 * @author Thomas Freese
 */
public class Email
{
    private String body;

    private String to;

    public Email()
    {
        super();
    }

    public Email(final String to, final String body)
    {
        super();

        this.to = to;
        this.body = body;
    }

    public String getBody()
    {
        return this.body;
    }

    public String getTo()
    {
        return this.to;
    }

    public void setBody(final String body)
    {
        this.body = body;
    }

    public void setTo(final String to)
    {
        this.to = to;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return String.format("Email{to=%s, body=%s}", getTo(), getBody());
    }
}
