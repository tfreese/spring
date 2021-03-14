package de.freese.spring.resilience;

import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;

/**
 * @author Thomas Freese
 */
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class SpringResilienceApplicationTests
{
    /**
     *
     */
    @Test
    void testContextLoads()
    {
        assertTrue(true);
    }
}
