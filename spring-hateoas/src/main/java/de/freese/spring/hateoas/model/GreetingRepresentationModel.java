// Created: 04.05.2016
package de.freese.spring.hateoas.model;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.hateoas.RepresentationModel;

/**
 * @author Thomas Freese
 */
@JsonIgnoreProperties
public class GreetingRepresentationModel extends RepresentationModel<GreetingRepresentationModel> {
    // @JsonUnwrapped
    // private final GreetingPojo pojo;

    @JsonProperty("greeting")
    private String message;

    @JsonCreator
    public GreetingRepresentationModel(final @JsonProperty("greeting") String message) {
        super();

        setMessage(message);
    }

    @Override
    public boolean equals(final Object o) {
        if (!(o instanceof final GreetingRepresentationModel that)) {
            return false;
        }

        if (!super.equals(o)) {
            return false;
        }

        return Objects.equals(message, that.message);
    }

    // @JsonGetter("greeting")
    public String getMessage() {
        return message;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), message);
    }

    // @JsonSetter("greeting")
    protected void setMessage(final String message) {
        this.message = Objects.requireNonNull(message, "message required");
    }
}
