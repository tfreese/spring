// Created: 25.04.2025
package de.spring.jooq;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * @author Thomas Freese
 */
@SpringBootTest
@ActiveProfiles("test")
class TestContext {
    @Test
    void testContextLoads() {
        assertTrue(true);
    }
}
