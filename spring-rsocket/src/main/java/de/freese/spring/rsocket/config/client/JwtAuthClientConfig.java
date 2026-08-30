package de.freese.spring.rsocket.config.client;

import org.springframework.context.annotation.Profile;
import org.springframework.security.rsocket.metadata.BearerTokenAuthenticationEncoder;
import org.springframework.stereotype.Component;

/**
 * @author Thomas Freese
 * @since 02.09.2021
 */
@Component
@Profile("jwt")
public class JwtAuthClientConfig extends AbstractClientConfig<BearerTokenAuthenticationEncoder> {
    @Override
    protected BearerTokenAuthenticationEncoder createAuthenticationEncoder() {
        return new BearerTokenAuthenticationEncoder();
    }
}
