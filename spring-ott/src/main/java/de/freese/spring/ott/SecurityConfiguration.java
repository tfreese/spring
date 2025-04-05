// Created: 05.04.2025
package de.freese.spring.ott;

import java.util.HashMap;
import java.util.Map;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.ott.InMemoryOneTimeTokenService;
import org.springframework.security.authentication.ott.JdbcOneTimeTokenService;
import org.springframework.security.authentication.ott.OneTimeTokenService;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

/**
 * @author Thomas Freese
 */
@Configuration
@EnableWebSecurity
@SuppressWarnings("java:S6437")
public class SecurityConfiguration {
    /**
     * See {@link JdbcOneTimeTokenService}.
     */
    @Bean
    OneTimeTokenService oneTimeTokenService() {
        return new InMemoryOneTimeTokenService();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        final Map<String, PasswordEncoder> encoders = new HashMap<>();
        encoders.put("noop", new PasswordEncoder() {
            @Override
            public String encode(final CharSequence rawPassword) {
                return rawPassword.toString();
            }

            @Override
            public boolean matches(final CharSequence rawPassword, final String encodedPassword) {
                return rawPassword.toString().equals(encodedPassword);
            }
        });

        return new DelegatingPasswordEncoder("noop", encoders);
    }

    @Bean
    SecurityFilterChain securityFilterChain(final HttpSecurity http) throws Exception {
        return http
                .authorizeHttpRequests(
                        auth -> auth
                                .requestMatchers("/ott/sent").permitAll()
                                .requestMatchers("/login/ott").permitAll()
                                .anyRequest().authenticated()
                )
                .formLogin(Customizer.withDefaults())   // Using form based authentication
                .oneTimeTokenLogin(Customizer.withDefaults())
                .build();
    }

    @Bean
    UserDetailsManager userDetailsManager(final PasswordEncoder passwordEncoder) {
        final InMemoryUserDetailsManager userDetailsManager = new InMemoryUserDetailsManager();
        userDetailsManager.createUser(User.withUsername("admin").passwordEncoder(passwordEncoder::encode).password("pw").roles("ADMIN", "USER").build());
        userDetailsManager.createUser(User.withUsername("user").passwordEncoder(passwordEncoder::encode).password("pw").roles("USER").build());

        return userDetailsManager;
    }
}
