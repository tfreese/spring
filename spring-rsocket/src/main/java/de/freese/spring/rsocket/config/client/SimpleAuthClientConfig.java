package de.freese.spring.rsocket.config.client;

import org.springframework.context.annotation.Profile;
import org.springframework.security.rsocket.metadata.SimpleAuthenticationEncoder;
import org.springframework.stereotype.Component;

/**
 * @author Thomas Freese
 * @since 02.09.2021
 */
@Component
@Profile("simple")
public class SimpleAuthClientConfig extends AbstractClientConfig<SimpleAuthenticationEncoder> {
    @Override
    protected SimpleAuthenticationEncoder createAuthenticationEncoder() {
        return new SimpleAuthenticationEncoder();
    }
}
