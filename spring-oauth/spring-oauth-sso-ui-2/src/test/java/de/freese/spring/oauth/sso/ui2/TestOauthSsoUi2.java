package de.freese.spring.oauth.sso.ui2;

import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @author Thomas Freese
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = OauthSsoUi2Application.class, webEnvironment = WebEnvironment.RANDOM_PORT)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Ignore
public class TestOauthSsoUi2
{
    /**
     * Erstellt ein neues {@link TestOauthSsoUi2} Object.
     */
    public TestOauthSsoUi2()
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
