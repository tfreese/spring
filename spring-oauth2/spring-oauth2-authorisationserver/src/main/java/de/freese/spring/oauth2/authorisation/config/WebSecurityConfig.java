/**
 * Created: 25.09.2018
 */

package de.freese.spring.oauth2.authorisation.config;

import javax.annotation.Resource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * @author Thomas Freese
 */
@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter
{
    /**
    *
    */
    @Resource
    private UserDetailsService myUserDetailsService = null;

    /**
    *
    */
    @Resource
    private PasswordEncoder passwordEncoder = null;

    /**
     * Erstellt ein neues {@link WebSecurityConfig} Object.
     */
    public WebSecurityConfig()
    {
        super();
    }

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
        auth//.jdbcAuthentication().userCache(userCache)
            .eraseCredentials(true)
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
        http
            .anonymous().disable()
            .csrf().disable()
            .authorizeRequests()
                .antMatchers("/auth/rest/**").authenticated() // Nur auf den /rest Pfad beschränken.
                .anyRequest().denyAll()
            .and()
                .formLogin().disable()
                .httpBasic().disable()
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.NEVER)

//            .antMatcher("/auth/rest/**")
//                .authorizeRequests()
//                    .anyRequest().authenticated()// Alle HTTP Methoden zulässig.
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
