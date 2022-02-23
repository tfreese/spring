// Created: 25.09.2018
package de.freese.spring.jwt.config.filterOnly;

import javax.servlet.Filter;

import de.freese.spring.jwt.token.JwtTokenProvider;
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
public class SecurityFilterOnlyConfig
{
    /**
     * @param authenticationProviderDao {@link AuthenticationProvider}
     *
     * @return {@link AuthenticationManager}
     */
    @Bean
    AuthenticationManager authenticationManager(final AuthenticationProvider authenticationProviderDao)
    {
        ProviderManager providerManager = new ProviderManager(authenticationProviderDao);
        // providerManager.setMessageSource(applicationContext); // Wird automatisch gemacht.
        providerManager.setEraseCredentialsAfterAuthentication(true);

        return providerManager;
    }

    /**
     * @param userDetailsService {@link UserDetailsService}
     * @param passwordEncoder {@link PasswordEncoder}
     * @param jwtTokenProvider {@link JwtTokenProvider}
     * @param authenticationEntryPoint {@link AuthenticationEntryPoint}
     *
     * @return {@link Filter}
     */
    @Bean
    Filter jwtRequestFilter(final UserDetailsService userDetailsService, final PasswordEncoder passwordEncoder, final JwtTokenProvider jwtTokenProvider,
                            final AuthenticationEntryPoint authenticationEntryPoint)
    {
        JwtRequestFilter jwtRequestFilter = new JwtRequestFilter();
        jwtRequestFilter.setUserDetailsService(userDetailsService);
        jwtRequestFilter.setPasswordEncoder(passwordEncoder);
        jwtRequestFilter.setJwtTokenProvider(jwtTokenProvider);
        jwtRequestFilter.setAuthenticationEntryPoint(authenticationEntryPoint);

        return jwtRequestFilter;
    }
}
