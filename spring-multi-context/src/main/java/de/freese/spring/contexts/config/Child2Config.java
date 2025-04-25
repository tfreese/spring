// Created: 25.04.2025
package de.freese.spring.contexts.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * @author Thomas Freese
 */
@Configuration
@ComponentScan("de.freese.spring.contexts.child2")
@Profile({"child2", "default"})
public class Child2Config {
}
