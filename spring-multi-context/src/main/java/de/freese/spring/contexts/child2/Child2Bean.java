package de.freese.spring.contexts.child2;

import java.util.Objects;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @author Thomas Freese
 * @since 25.04.2025
 */
@Component
public final class Child2Bean {
    private final String myVariable;

    public Child2Bean(@Value("${my.variable}") final String myVariable) {
        super();

        this.myVariable = Objects.requireNonNull(myVariable, "myVariable is required");
    }

    @Override
    public String toString() {
        return myVariable;
    }
}
