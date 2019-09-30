package de.freese.spring.oauth.sso.ui.config;

import org.springframework.boot.autoconfigure.security.oauth2.client.EnableOAuth2Sso;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

/**
 * @author Thomas Freese
 */
@SuppressWarnings("deprecation")
@EnableOAuth2Sso
@Configuration
public class UiSecurityConfig extends WebSecurityConfigurerAdapter
{
    /**
     * Erstellt ein neues {@link UiSecurityConfig} Object.
     */
    public UiSecurityConfig()
    {
        super();
    }

    /**
     * @see org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter#configure(org.springframework.security.config.annotation.web.builders.HttpSecurity)
     */
    @Override
    public void configure(final HttpSecurity http) throws Exception
    {
        // @formatter:off
        http.antMatcher("/**").authorizeRequests()
            .antMatchers("/", "/login**").permitAll()
            .anyRequest().authenticated()
            ;
        // @formatter:on
    }
}
