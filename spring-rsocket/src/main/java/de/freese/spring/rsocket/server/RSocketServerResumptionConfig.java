/**
 * Created: 11.03.2020
 */

package de.freese.spring.rsocket.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.rsocket.server.RSocketServerCustomizer;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import io.rsocket.core.RSocketServer;
import io.rsocket.core.Resume;

/**
 * @author Thomas Freese
 */
@Component
@Profile("server")
public class RSocketServerResumptionConfig implements RSocketServerCustomizer
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
     * Make the socket capable of resumption.<br>
     * By default, the Resume Session will have a duration of 120s,<br>
     * a timeout of 10s, and use the In Memory (volatile, non-persistent) session store.
     *
     * @see org.springframework.boot.rsocket.server.RSocketServerCustomizer#customize(io.rsocket.core.RSocketServer)
     */
    @Override
    public void customize(final RSocketServer rSocketServer)
    {
        LOGGER.info("Adding RSocket Server 'Resumption' Feature.");

        rSocketServer.resume(new Resume());
    }
}
