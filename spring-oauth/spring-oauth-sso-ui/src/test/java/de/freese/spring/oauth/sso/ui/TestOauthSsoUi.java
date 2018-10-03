package de.freese.spring.oauth.sso.ui;

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
@SpringBootTest(classes = OauthSsoUiApplication.class, webEnvironment = WebEnvironment.RANDOM_PORT)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Ignore
public class TestOauthSsoUi
{
    /**
     * Erstellt ein neues {@link TestOauthSsoUi} Object.
     */
    public TestOauthSsoUi()
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
