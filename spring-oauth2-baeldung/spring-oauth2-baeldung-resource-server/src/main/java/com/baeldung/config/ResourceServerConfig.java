package com.baeldung.config;

import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 *
 */
@EnableWebSecurity
public class ResourceServerConfig
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
            .mvcMatcher("/articles/**").authorizeRequests()
            .mvcMatchers("/articles/**").access("hasAuthority('SCOPE_articles.read')")
            .and()
            .oauth2ResourceServer()
            .jwt()
            ;
        // @formatter:on

        return http.build();
    }
}
