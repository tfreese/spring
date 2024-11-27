// Created: 25.09.2018
package de.freese.spring.jwt.config.defaultAuthProvider;

import jakarta.servlet.Filter;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.web.AuthenticationEntryPoint;

/**
 * Der {@link JwtRequestFilter} verwendet den Default-{@link AuthenticationProvider}.<br>
 * Siehe {@link DaoAuthenticationProvider}.<br>
 *
 * @author Thomas Freese
 */
@Configuration
@Profile("defaultAuthProvider")
public class SecurityDefaultAuthProviderConfig {
    @Bean
    AuthenticationManager authenticationManager(final AuthenticationProvider authenticationProviderDao) {
        final ProviderManager providerManager = new ProviderManager(authenticationProviderDao);
        // providerManager.setMessageSource(applicationContext); // Wird automatisch gemacht.
        providerManager.setEraseCredentialsAfterAuthentication(true);

        return providerManager;
    }

    @Bean
    @Primary
    Filter jwtRequestFilter(final AuthenticationManager authenticationManager, final AuthenticationEntryPoint authenticationEntryPoint) {
        return new JwtRequestFilter(authenticationManager, authenticationEntryPoint);

        // BasicAuthenticationEntryPoint entryPoint = new BasicAuthenticationEntryPoint();
        // entryPoint.setRealmName("Tommy");
        // jwtRequestFilter.setAuthenticationEntryPoint(entryPoint);
        //
        // return jwtRequestFilter;
    }
}
