package de.freese.spring.oauth.sso.auth;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.junit4.SpringRunner;
import de.freese.spring.oauth.sso.auth.OauthSsoAuthServerApplication;

/**
 * @author Thomas Freese
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = OauthSsoAuthServerApplication.class, webEnvironment = WebEnvironment.RANDOM_PORT)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TestOauthSsoAuthServer
{
    /**
     * Erstellt ein neues {@link TestOauthSsoAuthServer} Object.
     */
    public TestOauthSsoAuthServer()
    {
        super();
    }

    /**
     *
     */
    @Test
    public void contextLoads()
    {

    }
}
