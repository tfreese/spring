// Created: 25.09.2018
package de.freese.spring.jwt.config;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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

import de.freese.spring.jwt.token.JwtTokenUtils;

/**
 * @author Thomas Freese
 */
@Configuration
public class SecurityCommonConfig
{
    /**
     * @return {@link AuthenticationEntryPoint}
     */
    @Bean
    public AuthenticationEntryPoint authenticationEntryPoint()
    {
        return new RestAuthenticationEntryPoint();
    }

    /**
     * @param secretKey String
     * @param validityInMilliseconds long
     *
     * @return {@link JwtTokenUtils}
     */
    @Bean
    public JwtTokenUtils jwtTokenUtils(@Value("${security.jwt.token.secret-key:secret-key}") final String secretKey,
                                       @Value("${security.jwt.token.expire-length:3600000}") final long validityInMilliseconds)
    {
        String encodedSecretKey = Base64.getEncoder().encodeToString(secretKey.getBytes(StandardCharsets.UTF_8));

        // byte[] salt = KeyGenerators.secureRandom(16).generateKey();
        //
        // PBEKeySpec keySpec = new PBEKeySpec(this.secretKey.toCharArray(), salt, 1024, 256);
        // SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        // this.secretKey2 = factory.generateSecret(keySpec);

        return new JwtTokenUtils(encodedSecretKey, validityInMilliseconds);
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
     * @param passwordEncoder {@link PasswordEncoder}
     *
     * @return {@link UserDetailsService}
     */
    @Bean
    @ConditionalOnMissingBean(DataSource.class)
    public UserDetailsService userDetailsManager(final PasswordEncoder passwordEncoder)
    {
        InMemoryUserDetailsManager userDetailsManager = new InMemoryUserDetailsManager();

        // Wird im CreateUserRunner gemacht, Passwörter immer über den PasswordEncoder encoden und setzen !
        // userDetailsManager.createUser(User.withUsername("admin").password(passwordEncoder.encode("pass")).roles("ADMIN", "USER").build());
        // userDetailsManager.createUser(User.withUsername("user").password(passwordEncoder.encode("pass")).roles("USER").build());

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
