// Created: 25.09.2018
package de.freese.spring.oauth2.authorisation.config;

import javax.annotation.Resource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * @author Thomas Freese
 */
@Configuration
@Order(1)
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter
{
    /**
    *
    */
    @Resource
    private UserDetailsService myUserDetailsService;
    /**
    *
    */
    @Resource
    private PasswordEncoder passwordEncoder;

    /**
     * @see org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter#authenticationManagerBean()
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
            .eraseCredentials(false) // true löscht das Passwort, wird ein UserCache verwendet darf nicht das gleiche Objekt geliefert werden !
            .userDetailsService(userDetailsService())
            .passwordEncoder(this.passwordEncoder)
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
        http.authorizeRequests()
            .antMatchers("/login").permitAll()
            .antMatchers("/oauth/authorize").permitAll()
            .antMatchers("/oauth/token/revokeById/**").permitAll()
//            .antMatchers("/tokens/**").permitAll()
            .anyRequest().authenticated()
            .and()
                .formLogin().permitAll()
            .and()
                .csrf().disable()
                .anonymous().disable()
            ;
        // @formatter:on
    }

    /**
     * @see org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter#userDetailsService()
     */
    @Override
    @Bean
    public UserDetailsService userDetailsService()
    {
        return this.myUserDetailsService;
    }

    /**
     * @see org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter#userDetailsServiceBean()
     */
    @Override
    public UserDetailsService userDetailsServiceBean() throws Exception
    {
        return userDetailsService();
    }
}
