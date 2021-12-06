// Created: 25.09.2018
package de.freese.spring.jwt.config;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserCache;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.password.Pbkdf2PasswordEncoder;
import org.springframework.security.crypto.password.Pbkdf2PasswordEncoder.SecretKeyFactoryAlgorithm;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.www.BasicAuthenticationEntryPoint;

import de.freese.spring.jwt.token.JwtTokenFilter;

/**
 * @author Thomas Freese
 */
@Configuration
public class WebSecurityComponentsConfig
{
    /**
     * BasicAuthenticationEntryPoint liefert die volle HTML Fehler-Seite, dies ist bei REST nicht gewünscht.<br>
     * Aussedem wird die FilterChain weiter ausgeführt, wenn keine Credentials vorhanden sind.<br>
     * Dieser EntryPoint landet im {@link JwtTokenFilter}.
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
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());

            PrintWriter writer = response.getWriter();
            writer.println("HTTP Status 401 - " + authEx.getMessage());
            writer.flush();

            // response.sendError(HttpStatus.UNAUTHORIZED.value(), authEx.getMessage());
        }
    }

    /**
     * @return {@link AuthenticationEntryPoint}
     */
    @Bean
    public AuthenticationEntryPoint authenticationEntryPoint()
    {
        return new RestAuthenticationEntryPoint();
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
        encoders.put("pbkdf2", pbkdf2passwordEncoder);
        encoders.put("bcrypt", new BCryptPasswordEncoder(10));
        // encoders.put("scrypt", new SCryptPasswordEncoder()); // Benötigt BounyCastle
        // encoders.put("argon2", new Argon2PasswordEncoder()); // Benötigt BounyCastle
        encoders.put("noop", new PasswordEncoder()
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

        return new DelegatingPasswordEncoder("noop", encoders);
    }

    /**
     * @return {@link UserDetailsService}
     */
    @Bean
    @ConditionalOnMissingBean(DataSource.class)
    public UserDetailsService userDetailsManager()
    {
        InMemoryUserDetailsManager userDetailsManager = new InMemoryUserDetailsManager();

        // Wird im CreateUserRunner gemacht, Passwörter immer über den PasswordEncoder encoden und setzen !
        // userDetailsManager.createUser(User.withUsername("admin").password("pass").roles("ADMIN", "USER").build());
        // userDetailsManager.createUser(User.withUsername("user").password("pass").roles("USER").build());

        return userDetailsManager;
    }

    /**
     * @param dataSource {@link DataSource}
     * @param userCache {@link UserCache}
     *
     * @return {@link UserDetailsService}
     */
    @Bean
    @ConditionalOnBean(DataSource.class)
    public UserDetailsManager userDetailsManagerJdbc(final DataSource dataSource, final UserCache userCache)
    {
        JdbcUserDetailsManager userDetailsManager = new JdbcUserDetailsManager(dataSource);
        userDetailsManager.setUserCache(userCache);
        userDetailsManager.setUsersByUsernameQuery("select username, password, enabled from USER where username = ?");
        userDetailsManager.setAuthoritiesByUsernameQuery("select username, role from AUTHORITY where username = ?");

        return userDetailsManager;

        // JdbcDaoImpl jdbcDao = new JdbcDaoImpl();
        // jdbcDao.setDataSource(dataSource);
        // jdbcDao.setUsersByUsernameQuery("select username, password, enabled from USER where username = ?");
        // jdbcDao.setAuthoritiesByUsernameQuery("select username, role from AUTHORITY where username = ?");
        //
        // CachingUserDetailsService cachingUserDetailsService = new CachingUserDetailsService(jdbcDao);
        // cachingUserDetailsService.setUserCache(userCache);
        //
        // return cachingUserDetailsService;
    }
}
