// Created: 21.04.2019
package de.freese.spring.rsocket.model;

/**
 * @author Thomas Freese
 */
public class MessageResponse {
    private long index;

    private String message;

    public MessageResponse() {
        super();
    }

    public MessageResponse(final String message) {
        this(message, 0L);
    }

    public MessageResponse(final String message, final long index) {
        super();

        this.message = String.format("Hello %s", message);
        this.index = index;
    }

    public long getIndex() {
        return this.index;
    }

    public String getMessage() {
        return this.message;
    }

    public void setIndex(final long index) {
        this.index = index;
    }

    public void setMessage(final String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(getClass().getSimpleName());
        builder.append("[");
        builder.append("message=").append(this.message);
        builder.append(", index=").append(this.index);
        builder.append("]");

        return builder.toString();
    }
}
