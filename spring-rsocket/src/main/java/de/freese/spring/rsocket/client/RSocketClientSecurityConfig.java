/**
 * Created: 12.03.2020
 */

package de.freese.spring.rsocket.client;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.http.codec.json.Jackson2JsonEncoder;
import org.springframework.messaging.rsocket.RSocketStrategies;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.rsocket.metadata.SimpleAuthenticationEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;

/**
 * @author Thomas Freese
 */
@Configuration
@EnableWebFluxSecurity
@Profile("client")
public class RSocketClientSecurityConfig
{
    /**
     * Erstellt ein neues {@link RSocketClientSecurityConfig} Object.
     */
    public RSocketClientSecurityConfig()
    {
        super();
    }

    /**
     * @return {@link RSocketStrategies}
     */
    @Bean
    public RSocketStrategies rsocketStrategies()
    {
        //@formatter:off
        RSocketStrategies rSocketStrategies =  RSocketStrategies.builder()
                .encoder(new SimpleAuthenticationEncoder(), new Jackson2JsonEncoder())
                .decoder(new Jackson2JsonDecoder())
                .build()
                ;
        //@formatter:on

        return rSocketStrategies;
    }

    /**
     * @param http {@link ServerHttpSecurity}
     * @return {@link SecurityWebFilterChain}
     */
    @Bean
    public SecurityWebFilterChain securitygWebFilterChain(final ServerHttpSecurity http)
    {
        // Keine Sicherheit für den reaktiven REST-Service, alles erlauben.
        return http.authorizeExchange().anyExchange().permitAll().and().build();
    }
}
