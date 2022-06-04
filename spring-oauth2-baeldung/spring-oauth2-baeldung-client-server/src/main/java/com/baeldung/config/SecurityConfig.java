package com.baeldung.config;

import org.springframework.context.annotation.Bean;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 *
 */
@EnableWebSecurity
public class SecurityConfig
{
    /**
     * @param http {@link HttpSecurity}
     *
     * @return {@link SecurityFilterChain}
     *
     * @throws Exception Falls was schief geht.
     */
    @Bean
    SecurityFilterChain securityFilterChain(final HttpSecurity http) throws Exception
    {
        // @formatter:off
        http
            .authorizeRequests(authorizeRequests ->
                    authorizeRequests.anyRequest().authenticated()
            )
            .oauth2Login(oauth2Login ->
                    oauth2Login.loginPage("/oauth2/authorization/articles-client-oidc")
            )
            .oauth2Client(Customizer.withDefaults())
            ;
        // @formatter:on

        return http.build();
    }
}
