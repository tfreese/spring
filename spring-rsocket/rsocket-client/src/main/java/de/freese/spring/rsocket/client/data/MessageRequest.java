/**
 * Created: 21.04.2019
 */

package de.freese.spring.rsocket.client.data;

/**
 * @author Thomas Freese
 */
public class MessageRequest
{
    /**
     *
     */
    private String message = null;

    /**
     * Erstellt ein neues {@link MessageRequest} Object.
     */
    public MessageRequest()
    {
        super();
    }

    /**
     * Erstellt ein neues {@link MessageRequest} Object.
     *
     * @param message String
     */
    public MessageRequest(final String message)
    {
        super();

        this.message = message;
    }

    /**
     * @return String
     */
    public String getMessage()
    {
        return this.message;
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
        builder.append(getClass().getSimpleName());
        builder.append("[");
        builder.append("message=").append(this.message);
        builder.append("]");

        return builder.toString();
    }
}