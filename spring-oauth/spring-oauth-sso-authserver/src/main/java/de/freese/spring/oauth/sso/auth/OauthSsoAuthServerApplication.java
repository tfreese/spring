package de.freese.spring.oauth.sso.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;

/**
 * @author Thomas Freese
 */
@SpringBootApplication
@EnableResourceServer
public class OauthSsoAuthServerApplication
{
    /**
     * @param args String[]
     */
    public static void main(final String[] args)
    {
        SpringApplication.run(OauthSsoAuthServerApplication.class, args);
    }
}