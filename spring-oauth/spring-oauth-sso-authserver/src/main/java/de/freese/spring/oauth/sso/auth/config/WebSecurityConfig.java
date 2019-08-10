package de.freese.spring.oauth.sso.auth.config;

import java.util.HashMap;
import java.util.Map;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configurers.provisioning.InMemoryUserDetailsManagerConfigurer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.password.Pbkdf2PasswordEncoder;
import org.springframework.security.crypto.password.Pbkdf2PasswordEncoder.SecretKeyFactoryAlgorithm;

/**
 * @author Thomas Freese
 */
@Configuration
// @EnableGlobalMethodSecurity(securedEnabled = true)
@Order(1)
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
     * @return {@link AuthenticationManager}
     * @throws Exception Falls was schief geht.
     */
    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception
    {
        return super.authenticationManagerBean();
    }

    /**
     * @see org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter#configure(org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder)
     */
    @Override
    protected void configure(final AuthenticationManagerBuilder auth) throws Exception
    {
        // @formatter:off
        auth
            .eraseCredentials(true)
            .inMemoryAuthentication()
                .withUser("john")
                .password(passwordEncoder().encode("123"))
                .roles("USER")
            //.userDetailsService(userDetailsService())
            //.passwordEncoder(passwordEncoder())
            ;
        // @formatter:on
    }

    /**
     * @see org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter#configure(org.springframework.security.config.annotation.web.builders.HttpSecurity)
     */
    @Override
    protected void configure(final HttpSecurity http) throws Exception
    {
        // @formatter:off
        http.requestMatchers()
                .antMatchers("/login", "/oauth/authorize")
            .and()
            .authorizeRequests()
                .anyRequest()
                    .authenticated()
            .and()
            .formLogin()
                .permitAll()
            ;
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
        encoders.put("BCRYPT", new BCryptPasswordEncoder(10));
        encoders.put("PBKDF2", pbkdf2passwordEncoder);
        encoders.put("NOOP", new PasswordEncoder()
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

        DelegatingPasswordEncoder passwordEncoder = new DelegatingPasswordEncoder("NOOP", encoders);
        // passwordEncoder.setDefaultPasswordEncoderForMatches(NoOpPasswordEncoder.getInstance());

        return passwordEncoder;
    }

    /**
     * @see org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter#userDetailsService()
     */
    @Override
    @Bean
    protected UserDetailsService userDetailsService()
    {
        // @formatter:off
        UserDetailsService userDetailsService = new InMemoryUserDetailsManagerConfigurer<>()
                .passwordEncoder(passwordEncoder())
                .withUser("john").password(passwordEncoder().encode("123")).roles("USER")
                .and()
                .getUserDetailsService()
                ;
        // @formatter:on

        return userDetailsService;
    }

    // /**
    // * @see org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter#userDetailsServiceBean()
    // */
    // @Override
    // public UserDetailsService userDetailsServiceBean() throws Exception
    // {
    // return userDetailsService();
    // }
}
