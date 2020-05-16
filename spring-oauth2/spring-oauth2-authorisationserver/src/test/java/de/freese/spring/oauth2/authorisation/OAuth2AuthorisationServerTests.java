/**
 * Created: 31.10.2019
 */

package de.freese.spring.oauth2.authorisation;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * @author Thomas Freese
 */
// @ExtendWith(SpringExtension.class) // Ist bereits in SpringBootTest enthalten
@SpringBootTest
@TestMethodOrder(MethodOrderer.Alphanumeric.class)
@ActiveProfiles(
{
        "test", "memory"
})
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
