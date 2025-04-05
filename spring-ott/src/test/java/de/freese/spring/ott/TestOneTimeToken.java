// Created: 17.02.2019
package de.freese.spring.ott;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.ActiveProfiles;

/**
 * @author Thomas Freese
 */
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT, classes = SpringOneTimeTokenApplication.class)
@TestMethodOrder(MethodOrderer.MethodName.class)
@ActiveProfiles("test")
class TestOneTimeToken {
    @Test
    void testContextLoads() {
        assertTrue(true);
    }
}
