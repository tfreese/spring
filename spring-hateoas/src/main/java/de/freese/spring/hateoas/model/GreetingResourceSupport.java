// Erzeugt: 04.05.2016
package de.freese.spring.hateoas.model;

import java.util.Objects;
import org.springframework.hateoas.ResourceSupport;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Thomas Freese
 */
@JsonIgnoreProperties(ignoreUnknown = false)
public class GreetingResourceSupport extends ResourceSupport
{
    // /**
    // *
    // */
    // @JsonUnwrapped
    // private final GreetingPOJO pojo;
    /**
     *
     */
    @JsonProperty("greeting")
    private String message = null;

    // /**
    // * Erzeugt eine neue Instanz von {@link GreetingResourceSupport}.
    // */
    // public GreetingResourceSupport()
    // {
    // super();
    // }

    /**
     * Erzeugt eine neue Instanz von {@link GreetingResourceSupport}
     *
     * @param message String
     */
    @JsonCreator
    public GreetingResourceSupport(final @JsonProperty("greeting") String message)
    {
        super();

        setMessage(message);
    }

    /**
     * @return String
     */
    // @JsonGetter("greeting")
    public String getMessage()
    {
        return this.message;
    }

    /**
     * @param message String
     */
    // @JsonSetter("greeting")
    protected void setMessage(final String message)
    {
        this.message = Objects.requireNonNull(message, "message required");
    }
}
