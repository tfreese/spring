// Created: 25.09.2018
package de.freese.spring.jwt.config.filterOnly;

import jakarta.servlet.Filter;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;

import de.freese.spring.jwt.token.JwtTokenProvider;

/**
 * Der {@link JwtRequestFilter} verwendet keinen {@link AuthenticationProvider},<br>
 * sondern validiert das Token mit Passwort-Vergleich, Gültigkeit etc. selber und setzt es in den {@link SecurityContext}. <br>
 * DaoAuthenticationProvider<br>
 *
 * @author Thomas Freese
 */
@Configuration
@EnableWebSecurity
@Profile("filterOnly")
public class SecurityFilterOnlyConfig {
    @Bean
    AuthenticationManager authenticationManager(final AuthenticationProvider authenticationProviderDao) {
        final ProviderManager providerManager = new ProviderManager(authenticationProviderDao);
        // providerManager.setMessageSource(applicationContext); // Wird automatisch gemacht.
        providerManager.setEraseCredentialsAfterAuthentication(true);

        return providerManager;
    }

    @Bean
    Filter jwtRequestFilter(final UserDetailsService userDetailsService, final PasswordEncoder passwordEncoder, final JwtTokenProvider jwtTokenProvider, final AuthenticationEntryPoint authenticationEntryPoint) {
        final JwtRequestFilter jwtRequestFilter = new JwtRequestFilter();
        jwtRequestFilter.setUserDetailsService(userDetailsService);
        jwtRequestFilter.setPasswordEncoder(passwordEncoder);
        jwtRequestFilter.setJwtTokenProvider(jwtTokenProvider);
        jwtRequestFilter.setAuthenticationEntryPoint(authenticationEntryPoint);

        return jwtRequestFilter;
    }
}
