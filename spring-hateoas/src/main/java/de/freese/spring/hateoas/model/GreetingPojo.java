// Created: 04.05.2016
package de.freese.spring.hateoas.model;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Thomas Freese
 */
@JsonIgnoreProperties
// @JsonRootName("greeting")
// @XmlRootElement
// @Relation(value = "greeting", collectionRelation = "greetings")
public class GreetingPojo {
    @JsonProperty("greeting")
    private String message;

    @JsonCreator
    public GreetingPojo(final @JsonProperty("greeting") String message) {
        super();

        setMessage(message);
    }

    public String getMessage() {
        return this.message;
    }

    public void setMessage(final String message) {
        this.message = Objects.requireNonNull(message, "message required");
    }
}
