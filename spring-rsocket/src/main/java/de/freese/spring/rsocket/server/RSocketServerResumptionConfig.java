/**
 * Created: 11.03.2020
 */

package de.freese.spring.rsocket.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.rsocket.server.ServerRSocketFactoryProcessor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import io.rsocket.RSocketFactory;

/**
 * @author Thomas Freese
 */
@Component
@Profile("server")
public class RSocketServerResumptionConfig implements ServerRSocketFactoryProcessor
{
    /**
     *
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(RSocketServerResumptionConfig.class);

    /**
     * Erstellt ein neues {@link RSocketServerResumptionConfig} Object.
     */
    public RSocketServerResumptionConfig()
    {
        super();
    }

    /**
     * In this method we can configure the ServerRSocketFactory.<br>
     * In this case, we are switching on the 'resumption' feature with 'resume()'.<br>
     * By default, the Resume Session will have a duration of 120s, a timeout of 10s, and use the In Memory (volatile, non-persistent) session store.
     *
     * @see org.springframework.boot.rsocket.server.ServerRSocketFactoryProcessor#process(io.rsocket.RSocketFactory.ServerRSocketFactory)
     */
    @Override
    public RSocketFactory.ServerRSocketFactory process(final RSocketFactory.ServerRSocketFactory factory)
    {
        LOGGER.info("Adding RSocket Server 'Resumption' Feature.");

        return factory.resume(); // By default duration=120s and store=InMemory and timeout=10s
    }
}
