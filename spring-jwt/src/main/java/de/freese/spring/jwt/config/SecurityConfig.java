// Created: 25.09.2018
package de.freese.spring.jwt.config;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.Filter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserCache;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.password.Pbkdf2PasswordEncoder;
import org.springframework.security.crypto.password.Pbkdf2PasswordEncoder.SecretKeyFactoryAlgorithm;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.www.BasicAuthenticationEntryPoint;

import de.freese.spring.jwt.token.JwtTokenAuthenticationProvider;
import de.freese.spring.jwt.token.JwtTokenFilter;
import de.freese.spring.jwt.token.JwtTokenProvider;

/**
 * @author Thomas Freese
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter
{
    /**
     * BasicAuthenticationEntryPoint liefert die volle HTML Fehler-Seite, dies ist bei REST nicht gewünscht.<br>
     * Aussedem wird die FilterChain weiter ausgeführt, wenn keine Credentials vorhanden sind.
     *
     * @author Thomas Freese
     */
    private static class RestAuthenticationEntryPoint extends BasicAuthenticationEntryPoint
    {
        /**
         * @see org.springframework.security.web.authentication.www.BasicAuthenticationEntryPoint#afterPropertiesSet()
         */
        @Override
        public void afterPropertiesSet()
        {
            setRealmName("Tommy");

            super.afterPropertiesSet();
        }

        /**
         * @see org.springframework.security.web.authentication.www.BasicAuthenticationEntryPoint#commence(javax.servlet.http.HttpServletRequest,
         *      javax.servlet.http.HttpServletResponse, org.springframework.security.core.AuthenticationException)
         */
        @Override
        public void commence(final HttpServletRequest request, final HttpServletResponse response, final AuthenticationException authEx) throws IOException
        {
            response.addHeader("WWW-Authenticate", "Basic realm=" + getRealmName());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding("UTF-8");

            PrintWriter writer = response.getWriter();
            writer.println("HTTP Status 401 - " + authEx.getMessage());
        }
    }

    /**
     *
     */
    @Resource
    private JwtTokenProvider jwtTokenProvider;
    /**
     *
     */
    @Resource
    private UserCache userCache;

    /**
     * @return {@link AuthenticationEntryPoint}
     */
    @Bean
    public AuthenticationEntryPoint authenticationEntryPoint()
    {
        RestAuthenticationEntryPoint authenticationEntryPoint = new RestAuthenticationEntryPoint();

        return authenticationEntryPoint;
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
        auth
            .eraseCredentials(true)
            .authenticationProvider(jwtAuthenticationProvider())
            .userDetailsService(userDetailsService()) // Erzeugt DaoAuthenticationProvider
                .passwordEncoder(passwordEncoder())
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
        jwtAuthenticationProvider.setPasswordEncoder(passwordEncoder());
        jwtAuthenticationProvider.setTokenProvider(this.jwtTokenProvider);

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
        JwtTokenFilter jwtTokenFilter = new JwtTokenFilter();
        jwtTokenFilter.setAuthenticationManager(authenticationManager());
        jwtTokenFilter.setAuthenticationEntryPoint(authenticationEntryPoint());

        return jwtTokenFilter;
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
        encoders.put("PLAIN", new PasswordEncoder()
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

        DelegatingPasswordEncoder passwordEncoder = new DelegatingPasswordEncoder("PLAIN", encoders);
        // passwordEncoder.setDefaultPasswordEncoderForMatches(NoOpPasswordEncoder.getInstance());

        return passwordEncoder;
    }

    /**
     * @see org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter#userDetailsService()
     */
    @Override
    @Bean
    public UserDetailsService userDetailsService()
    {
        InMemoryUserDetailsManager userDetailsManager = new InMemoryUserDetailsManager();
        // userDetailsManager.createUser(User.withUsername("admin").password("pass").roles("ADMIN", "USER").build());
        // userDetailsManager.createUser(User.withUsername("user").password("pass").roles("USER").build());

        UserDetailsService userDetailsService = userDetailsManager;

        return userDetailsService;
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
