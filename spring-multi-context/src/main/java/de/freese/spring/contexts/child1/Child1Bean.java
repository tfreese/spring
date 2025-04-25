// Created: 25.04.2025
package de.freese.spring.contexts.child1;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @author Thomas Freese
 */
@Component
public final class Child1Bean {
    @Value("${my.variable}")
    private String myVariable;

    @Override
    public String toString() {
        return myVariable;
    }
}
