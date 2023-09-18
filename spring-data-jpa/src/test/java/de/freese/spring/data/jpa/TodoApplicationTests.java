// Created: 16.08.23
package de.freese.spring.data.jpa;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * @author Thomas Freese
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
class TodoApplicationTests {

    @Test
    void contextLoads() {
        // Empty
        Assertions.assertTrue(true);
    }
}
