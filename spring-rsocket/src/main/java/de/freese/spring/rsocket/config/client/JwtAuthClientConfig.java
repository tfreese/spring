// Created: 02.09.2021
package de.freese.spring.rsocket.config.client;

import org.springframework.context.annotation.Profile;
import org.springframework.core.codec.Encoder;
import org.springframework.security.rsocket.metadata.BearerTokenAuthenticationEncoder;
import org.springframework.stereotype.Component;

/**
 * @author Thomas Freese
 */
@Component
@Profile("jwt")
public class JwtAuthClientConfig extends AbstractClientConfig
{
    /**
     * @see de.freese.spring.rsocket.config.client.AbstractClientConfig#createAuthenticationEncoder()
     */
    @Override
    protected Encoder<?> createAuthenticationEncoder()
    {
        return new BearerTokenAuthenticationEncoder();
    }
}
