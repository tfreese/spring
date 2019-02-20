/**
 * Created: 17.02.2019
 */

package de.freese.spring.ldap.unboundid.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.password.LdapShaPasswordEncoder;

/**
 * @author Thomas Freese
 */
@Configuration
public class WebSecurityConfig extends WebSecurityConfigurerAdapter
{
    /**
     * Erstellt ein neues {@link WebSecurityConfig} Object.
     */
    public WebSecurityConfig()
    {
        super();
    }

    /**
     * @see org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter#configure(org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder)
     */
    @Override
    public void configure(final AuthenticationManagerBuilder auth) throws Exception
    {
        // @formatter:off
        auth.ldapAuthentication()
            .userDnPatterns("uid={0},ou=people")
            .groupSearchBase("ou=groups")
            .contextSource()
                .url("ldap://localhost:8389/dc=springframework,dc=org")
            .and()
            .passwordCompare()
                .passwordEncoder(new LdapShaPasswordEncoder())
                .passwordAttribute("userPassword");
        // @formatter:on
    }

    @Override
    protected void configure(final HttpSecurity http) throws Exception
    {
        // @formatter:off
        http.authorizeRequests()
            .anyRequest()
            .fullyAuthenticated()
            .and()
            .formLogin();
        // @formatter:on
    }
}
