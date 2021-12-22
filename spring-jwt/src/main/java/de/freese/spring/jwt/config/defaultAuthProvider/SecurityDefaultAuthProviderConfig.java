// Created: 25.09.2018
package de.freese.spring.jwt.config.defaultAuthProvider;

import javax.annotation.Resource;
import javax.servlet.Filter;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import de.freese.spring.jwt.token.JwtTokenProvider;

/**
 * Der {@link JwtRequestFilter} verwendet den Default-{@link AuthenticationProvider}.<br>
 * Siehe {@link DaoAuthenticationProvider}.
 *
 * @author Thomas Freese
 */
@Configuration
@EnableWebSecurity
@Profile(
{
        "defaultAuthProvider", "default"
})
public class SecurityDefaultAuthProviderConfig extends WebSecurityConfigurerAdapter
{
    /**
     *
     */
    @Resource
    private AuthenticationEntryPoint authenticationEntryPoint;
    /**
     *
     */
    @Resource
    private JwtTokenProvider jwtTokenProvider;
    /**
    *
    */
    @Resource
    private PasswordEncoder passwordEncoder;
    /**
     *
     */
    @Resource
    private UserDetailsManager userDetailsManager;

    /**
     * Erzeugt mit einem {@link UserDetailsService} einen {@link DaoAuthenticationProvider}.
     *
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
            .eraseCredentials(true)
            .userDetailsService(userDetailsService()) // Erzeugt DaoAuthenticationProvider
                .passwordEncoder(this.passwordEncoder)
            ;
        // @formatter:on

        // 2. AuthenticationProvider
        // z.b. auth.ldapAuthentication()
    }

    /**
     * @see org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter#configure(org.springframework.security.config.annotation.web.builders.HttpSecurity)
     */
    @Override
    protected void configure(final HttpSecurity http) throws Exception
    {
        // @formatter:off
        http//.authorizeRequests().anyRequest().permitAll()
            .anonymous().disable()
            .csrf().disable()
            .formLogin().disable()
            .httpBasic().disable()
            .authorizeRequests()
                //.antMatchers("/users/login").access("hasRole('ROLE_USER') or hasRole('ROLE_ADMIN')")
                //.antMatchers("/users/register").hasRole("ADMIN")
                .anyRequest().authenticated()
            .and()
                .exceptionHandling().authenticationEntryPoint(this.authenticationEntryPoint)
            .and()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
//                .apply(new JwtTokenFilterConfigurer(this.jwtTokenProvider))
                .addFilterBefore(jwtTokenFilter(), UsernamePasswordAuthenticationFilter.class)

            ;
        // @formatter:on
    }

    /**
     * @see org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter#configure(org.springframework.security.config.annotation.web.builders.WebSecurity)
     */
    @Override
    public void configure(final WebSecurity webSecurity)
    {
        // Pfade ohne Sicherheits-Prüfung.
        // @formatter:off
        webSecurity.ignoring()
            // Für swagger
            .antMatchers("/swagger-ui.html")
            .antMatchers("/webjars/**")
            .antMatchers("/v2/api-docs")
            .antMatchers("/swagger-resources/**")

            .antMatchers("/users/login")

            // Un-secure H2 Database (for testing purposes, H2 console shouldn't be unprotected in production)
            //.antMatchers("/h2-console/**/**")
            ;
        // @formatter:on
    }

    /**
     * @return {@link Filter}
     *
     * @throws Exception Falls was schief geht.
     */
    @Bean
    public Filter jwtTokenFilter() throws Exception
    {
        JwtRequestFilter jwtTokenFilter = new JwtRequestFilter();
        jwtTokenFilter.setAuthenticationManager(authenticationManager());
        jwtTokenFilter.setAuthenticationEntryPoint(this.authenticationEntryPoint);
        jwtTokenFilter.setJwtTokenProvider(this.jwtTokenProvider);

        // BasicAuthenticationEntryPoint entryPoint = new BasicAuthenticationEntryPoint();
        // entryPoint.setRealmName("Tommy");
        // jwtTokenFilter.setAuthenticationEntryPoint(entryPoint);

        return jwtTokenFilter;
    }

    /**
     * @see org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter#userDetailsService()
     */
    @Override
    protected UserDetailsService userDetailsService()
    {
        return this.userDetailsManager;
    }

    // /**
    // * @see org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter#userDetailsServiceBean()
    // */
    // @Override
    // public UserDetailsService userDetailsServiceBean() throws Exception
    // {
    // return this.userDetailsManager;
    // }
}
