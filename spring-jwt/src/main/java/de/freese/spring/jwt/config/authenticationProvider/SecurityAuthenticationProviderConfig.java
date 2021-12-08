// Created: 25.09.2018
package de.freese.spring.jwt.config.authenticationProvider;

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
import org.springframework.security.core.userdetails.UserCache;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import de.freese.spring.jwt.token.JwtTokenUtils;

/**
 * @author Thomas Freese
 */
@Configuration
@EnableWebSecurity
@Profile("AuthenticationProvider")
public class SecurityAuthenticationProviderConfig extends WebSecurityConfigurerAdapter
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
    private JwtTokenUtils jwtTokenUtils;
    /**
    *
    */
    @Resource
    private PasswordEncoder passwordEncoder;
    /**
     *
     */
    @Resource
    private UserCache userCache;
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
            .authenticationProvider(jwtAuthenticationProvider())
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
                //.antMatchers("/users/login").permitAll()
                //.antMatchers("/users/register").permitAll()
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
                //.antMatchers("/configuration/**")
                //.antMatchers("/public")

                .antMatchers("/users/login")

                // Un-secure H2 Database (for testing purposes, H2 console shouldn't be unprotected in production)
                .antMatchers("/h2-console/**/**")
                ;
        // @formatter:on
    }

    /**
     * @return {@link AuthenticationProvider}
     */
    @Bean
    public AuthenticationProvider jwtAuthenticationProvider()
    {
        JwtTokenAuthenticationProvider jwtAuthenticationProvider = new JwtTokenAuthenticationProvider();
        jwtAuthenticationProvider.setUserDetailsService(userDetailsService());
        jwtAuthenticationProvider.setUserCache(this.userCache);
        jwtAuthenticationProvider.setPasswordEncoder(this.passwordEncoder);
        jwtAuthenticationProvider.setJwtTokenUtils(this.jwtTokenUtils);

        return jwtAuthenticationProvider;
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
