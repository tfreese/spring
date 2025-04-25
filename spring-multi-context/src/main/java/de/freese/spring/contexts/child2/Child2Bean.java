// Created: 25.04.2025
package de.freese.spring.contexts.child2;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @author Thomas Freese
 */
@Component
public final class Child2Bean {
    @Value("${my.variable}")
    private String myVariable;

    @Override
    public String toString() {
        return myVariable;
    }
}
