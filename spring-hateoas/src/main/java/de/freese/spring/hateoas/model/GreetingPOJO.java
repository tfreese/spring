// Erzeugt: 04.05.2016
package de.freese.spring.hateoas.model;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Thomas Freese
 */
@JsonIgnoreProperties(ignoreUnknown = false)
// @JsonRootName("greeting")
// @XmlRootElement
// @Relation(value = "greeting", collectionRelation = "greetings")
public class GreetingPOJO
{
    /**
     *
     */
    @JsonProperty("greeting")
    private String message = null;

    /**
     * Erzeugt eine neue Instanz von {@link GreetingPOJO}
     *
     * @param message String
     */
    @JsonCreator
    public GreetingPOJO(final @JsonProperty("greeting") String message)
    {
        super();

        setMessage(message);
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
        this.message = Objects.requireNonNull(message, "message required");
    }
}
