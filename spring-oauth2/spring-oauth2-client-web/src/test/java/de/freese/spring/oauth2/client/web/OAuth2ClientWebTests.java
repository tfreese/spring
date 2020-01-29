/**
 * Created: 31.10.2019
 */

package de.freese.spring.oauth2.client.web;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * @author Thomas Freese
 */
@SpringBootTest
@TestMethodOrder(MethodOrderer.Alphanumeric.class)
@ActiveProfiles("test")
public class OAuth2ClientWebTests
{
    /**
     *
     */
    @Test
    public void contextLoads()
    {
    }
}
