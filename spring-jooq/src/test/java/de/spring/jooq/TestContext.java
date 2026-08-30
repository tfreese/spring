package de.spring.jooq;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * @author Thomas Freese
 * @since 25.04.2025
 */
@SpringBootTest
@ActiveProfiles("test")
class TestContext {
    @Test
    void testContextLoads() {
        assertTrue(true);
    }
}
