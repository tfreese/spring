/**
 * Created: 31.01.2019
 */
package de.freese.spring.messaging.jms;

/**
 * @author Thomas Freese
 */
public class Email
{
    /**
     *
     */
    private String body;

    /**
     *
     */
    private String to;

    /**
     * Erstellt ein neues {@link Email} Object.
     */
    public Email()
    {
        super();
    }

    /**
     * Erstellt ein neues {@link Email} Object.
     *
     * @param to String
     * @param body String
     */
    public Email(final String to, final String body)
    {
        super();

        this.to = to;
        this.body = body;
    }

    /**
     * @return String
     */
    public String getBody()
    {
        return this.body;
    }

    /**
     * @return String
     */
    public String getTo()
    {
        return this.to;
    }

    /**
     * @param body String
     */
    public void setBody(final String body)
    {
        this.body = body;
    }

    /**
     * @param to String
     */
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
