// Created: 25.09.2018
package de.freese.spring.jwt.config.defaultAuthProvider;

import javax.servlet.Filter;

import de.freese.spring.jwt.token.JwtTokenProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.AuthenticationEntryPoint;

/**
 * Der {@link JwtRequestFilter} verwendet den Default-{@link AuthenticationProvider}.<br>
 * Siehe {@link DaoAuthenticationProvider}.<br>
 *
 * @author Thomas Freese
 */
@Configuration
@EnableWebSecurity
@Profile(
        {
                "defaultAuthProvider", "default"
        })
public class SecurityDefaultAuthProviderConfig
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
     * @param authenticationManager {@link AuthenticationManager}
     * @param authenticationEntryPoint {@link AuthenticationEntryPoint}
     * @param jwtTokenProvider {@link JwtTokenProvider}
     *
     * @return {@link Filter}
     *
     * @throws Exception Falls was schiefgeht.
     */
    @Bean
    Filter jwtRequestFilter(final AuthenticationManager authenticationManager, final AuthenticationEntryPoint authenticationEntryPoint,
                            final JwtTokenProvider jwtTokenProvider)
            throws Exception
    {
        JwtRequestFilter jwtRequestFilter = new JwtRequestFilter();
        jwtRequestFilter.setAuthenticationManager(authenticationManager);
        jwtRequestFilter.setAuthenticationEntryPoint(authenticationEntryPoint);
        jwtRequestFilter.setJwtTokenProvider(jwtTokenProvider);

        // BasicAuthenticationEntryPoint entryPoint = new BasicAuthenticationEntryPoint();
        // entryPoint.setRealmName("Tommy");
        // jwtRequestFilter.setAuthenticationEntryPoint(entryPoint);

        return jwtRequestFilter;
    }
}
