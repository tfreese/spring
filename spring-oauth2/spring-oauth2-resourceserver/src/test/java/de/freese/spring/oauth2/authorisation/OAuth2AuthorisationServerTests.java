/**
 * Created: 31.10.2019
 */

package de.freese.spring.oauth2.authorisation;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @author Thomas Freese
 */
@RunWith(SpringRunner.class)
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
