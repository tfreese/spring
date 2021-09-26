// Created: 17.02.2019
package de.freese.spring.ldap.unboundid.config;

import java.util.HashMap;
import java.util.Map;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.password.Pbkdf2PasswordEncoder;
import org.springframework.security.crypto.password.Pbkdf2PasswordEncoder.SecretKeyFactoryAlgorithm;

/**
 * @author Thomas Freese
 */
@Configuration
public class WebSecurityConfig extends WebSecurityConfigurerAdapter
{
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
                .passwordEncoder(passwordEncoder())
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

    /**
     * @return {@link PasswordEncoder}
     */
    @Bean
    public PasswordEncoder passwordEncoder()
    {
        Pbkdf2PasswordEncoder pbkdf2passwordEncoder = new Pbkdf2PasswordEncoder("mySecret");
        pbkdf2passwordEncoder.setAlgorithm(SecretKeyFactoryAlgorithm.PBKDF2WithHmacSHA512);
        pbkdf2passwordEncoder.setEncodeHashAsBase64(false);

        Map<String, PasswordEncoder> encoders = new HashMap<>();
        encoders.put("PBKDF2", pbkdf2passwordEncoder);
        encoders.put("BCRYPT", new BCryptPasswordEncoder(10));
        // encoders.put("SSHA", new LdapShaPasswordEncoder());
        // encoders.put("", NoOpPasswordEncoder.getInstance());
        encoders.put("", new PasswordEncoder()
        {
            @Override
            public String encode(final CharSequence rawPassword)
            {
                return rawPassword.toString();
            }

            @Override
            public boolean matches(final CharSequence rawPassword, final String encodedPassword)
            {
                return rawPassword.toString().equals(encodedPassword);
            }
        });

        DelegatingPasswordEncoder passwordEncoder = new DelegatingPasswordEncoder("", encoders);
        // passwordEncoder.setDefaultPasswordEncoderForMatches(NoOpPasswordEncoder.getInstance());

        return passwordEncoder;
    }
}
