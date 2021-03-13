// Erzeugt: 04.05.2016
package de.freese.spring.hateoas.model;

import java.util.Objects;
import org.springframework.hateoas.RepresentationModel;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Thomas Freese
 */
@JsonIgnoreProperties(ignoreUnknown = false)
public class GreetingRepresentationModel extends RepresentationModel<GreetingRepresentationModel>
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
    private String message;

    /**
     * Erzeugt eine neue Instanz von {@link GreetingRepresentationModel}
     *
     * @param message String
     */
    @JsonCreator
    public GreetingRepresentationModel(final @JsonProperty("greeting") String message)
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
