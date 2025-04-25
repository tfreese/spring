// Created: 25.04.2025
package de.freese.spring.contexts;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.ContextHierarchy;
import org.springframework.test.context.TestPropertySource;

import de.freese.spring.contexts.config.Child1Config;
import de.freese.spring.contexts.config.Child2Config;
import de.freese.spring.contexts.config.ParentConfig;

/**
 * @author Thomas Freese
 */
@SpringBootTest(webEnvironment = WebEnvironment.NONE)
@ContextHierarchy({
        @ContextConfiguration(classes = ParentConfig.class),
        @ContextConfiguration(classes = Child1Config.class),
        @ContextConfiguration(classes = Child2Config.class)
})
@TestPropertySource(properties = {"spring.config.location=classpath:application-profiles.yml"})
@ActiveProfiles({"parent", "child1", "child2"})
class TestMultiContext {
    @Test
    void testContextLoads() {
        assertTrue(true);
    }
}
