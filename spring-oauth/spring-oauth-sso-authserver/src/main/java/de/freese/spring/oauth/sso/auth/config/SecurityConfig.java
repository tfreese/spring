package de.freese.spring.oauth.sso.auth.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * @author Thomas Freese
 */
@Configuration
// @EnableGlobalMethodSecurity(securedEnabled = true)
@Order(1)
public class SecurityConfig extends WebSecurityConfigurerAdapter
{
    /**
     * Erstellt ein neues {@link SecurityConfig} Object.
     */
    public SecurityConfig()
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
        return NoOpPasswordEncoder.getInstance();
    }

    // /**
    // * @see org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter#userDetailsService()
    // */
    // @Override
    // @Bean
    // protected UserDetailsService userDetailsService()
    // {
//        // @formatter:off
//        UserDetailsService userDetailsService = new InMemoryUserDetailsManagerConfigurer<>()
//                .passwordEncoder(passwordEncoder())
//                .withUser("john")
//                    .password(passwordEncoder().encode("123"))
//                    .roles("USER")
//                .and()
//                .getUserDetailsService()
//                ;
//        // @formatter:on
    //
    // return userDetailsService;
    // }

    // /**
    // * @see org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter#userDetailsServiceBean()
    // */
    // @Override
    // public UserDetailsService userDetailsServiceBean() throws Exception
    // {
    // return userDetailsService();
    // }
}
