/**
 * Created: 31.10.2019
 */

package de.freese.spring.oauth2.authorisation;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

/**
 * @author Thomas Freese
 */
@ExtendWith(SpringExtension.class)
@TestMethodOrder(MethodOrderer.Alphanumeric.class)
@SpringBootTest
@ActiveProfiles("test, memory")
public class OAuth2AuthorisationServerTests
{
    /**
     *
     */
    @Test
    public void contextLoads()
    {
    }
}
