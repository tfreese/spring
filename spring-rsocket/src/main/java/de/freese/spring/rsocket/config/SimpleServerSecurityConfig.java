// Created: 12.03.2020
package de.freese.spring.rsocket.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.rsocket.EnableRSocketSecurity;
import org.springframework.security.config.annotation.rsocket.RSocketSecurity;
import org.springframework.security.rsocket.core.PayloadSocketAcceptorInterceptor;

/**
 * @author Thomas Freese
 */
@Configuration
@EnableRSocketSecurity
@EnableReactiveMethodSecurity
@Profile("simple")
public class SimpleServerSecurityConfig extends AbstractServerSecurityConfig
{
    /**
     * @param security {@link RSocketSecurity}
     *
     * @return {@link PayloadSocketAcceptorInterceptor}
     */
    @Bean
    PayloadSocketAcceptorInterceptor authentication(final RSocketSecurity security)
    {
        //@formatter:off
        security.authorizePayload(authorize ->
            authorize
                    // User muss ROLE_SETUP haben um Verbindung zum Server herzustellen.
                    //.setup().hasRole("SETUP")
                    // User muss ROLE_ADMIN haben für das Absetzen der Requests auf die End-Punkte.
                    //.route("greet/*").hasRole("ADMIN")
                    .anyRequest().authenticated()
                    .anyExchange().authenticated()
        )
        .simpleAuthentication(Customizer.withDefaults())
        ;
        //@formatter:on

        return security.build();
    }
}
