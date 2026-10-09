package de.freese.spring.contexts.parent;

import java.util.Objects;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @author Thomas Freese
 * @since 25.04.2025
 */
@Component
public final class ParentBean {
    private final String myVariable;

    public ParentBean(@Value("${my.variable}") final String myVariable) {
        super();

        this.myVariable = Objects.requireNonNull(myVariable, "myVariable is required");
    }

    @Override
    public String toString() {
        return myVariable;
    }
}
