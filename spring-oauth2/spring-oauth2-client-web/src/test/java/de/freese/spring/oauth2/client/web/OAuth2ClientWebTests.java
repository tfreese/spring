/**
 * Created: 31.10.2019
 */
package de.freese.spring.oauth2.client.web;

import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * @author Thomas Freese
 */
@SpringBootTest
@TestMethodOrder(MethodOrderer.MethodName.class)
@ActiveProfiles("test")
class OAuth2ClientWebTests
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
