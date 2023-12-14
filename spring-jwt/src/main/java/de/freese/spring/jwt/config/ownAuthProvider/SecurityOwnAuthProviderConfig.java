// Created: 25.09.2018
package de.freese.spring.jwt.config.ownAuthProvider;

import jakarta.servlet.Filter;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;

import de.freese.spring.jwt.token.JwtTokenProvider;

/**
 * Der {@link JwtRequestFilter} verwendet den {@link JwtTokenAuthenticationProvider}.<br>
 *
 * @author Thomas Freese
 */
@Configuration
@EnableWebSecurity
@Profile("ownAuthProvider")
public class SecurityOwnAuthProviderConfig {
    @Bean
    AuthenticationManager authenticationManager(final AuthenticationProvider authenticationProviderJwt, final AuthenticationProvider authenticationProviderDao) {
        final ProviderManager providerManager = new ProviderManager(authenticationProviderJwt, authenticationProviderDao);
        // providerManager.setMessageSource(applicationContext); // Wird automatisch gemacht.
        providerManager.setEraseCredentialsAfterAuthentication(true);

        return providerManager;
    }

    @Bean
    AuthenticationProvider authenticationProviderJwt(final PasswordEncoder passwordEncoder, final UserDetailsService userDetailsService, final JwtTokenProvider jwtTokenProvider) {
        final JwtTokenAuthenticationProvider jwtAuthenticationProvider = new JwtTokenAuthenticationProvider();
        // jwtAuthenticationProvider.setMessageSource(applicationContext); // Wird automatisch gemacht.
        jwtAuthenticationProvider.setPasswordEncoder(passwordEncoder);
        jwtAuthenticationProvider.setUserDetailsService(userDetailsService);
        jwtAuthenticationProvider.setJwtTokenProvider(jwtTokenProvider);

        // Böse Falle !
        // Der UserCache im AuthenticationProvider behält die UserDetails der User.
        // Bei diesen werden aber die Passwörter aus Sicherheitsgründen im ProviderManager entfernt.
        // Dadurch ist ein 2. Login dann nicht mehr möglich, es folgt NullPointer wegen UserDetails.getPassword = null
        // jwtAuthenticationProvider.setUserCache(this.userCache);

        return jwtAuthenticationProvider;
    }

    @Bean
    Filter jwtRequestFilter(final AuthenticationManager authenticationManager, final AuthenticationEntryPoint authenticationEntryPoint) throws Exception {
        final JwtRequestFilter jwtRequestFilter = new JwtRequestFilter();
        jwtRequestFilter.setAuthenticationManager(authenticationManager);
        jwtRequestFilter.setAuthenticationEntryPoint(authenticationEntryPoint);

        // BasicAuthenticationEntryPoint entryPoint = new BasicAuthenticationEntryPoint();
        // entryPoint.setRealmName("Tommy");
        // jwtRequestFilter.setAuthenticationEntryPoint(entryPoint);

        return jwtRequestFilter;
    }
}
