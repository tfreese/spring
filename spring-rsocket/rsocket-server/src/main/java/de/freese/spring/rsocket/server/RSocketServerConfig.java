// Created: 11.03.2020
package de.freese.spring.rsocket.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.rsocket.RSocketServerAutoConfiguration;
import org.springframework.boot.rsocket.netty.NettyRSocketServerFactory;
import org.springframework.boot.rsocket.server.RSocketServerCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.http.client.reactive.ReactorResourceFactory;
import org.springframework.stereotype.Component;

import io.rsocket.core.RSocketServer;
import io.rsocket.core.Resume;
import io.rsocket.frame.decoder.PayloadDecoder;
import reactor.netty.resources.LoopResources;

/**
 * @author Thomas Freese
 *
 * @see NettyRSocketServerFactory
 * @see RSocketServerAutoConfiguration
 */
@Component
public class RSocketServerConfig implements RSocketServerCustomizer
{
    /**
     *
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(RSocketServerConfig.class);

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

        rSocketServer.payloadDecoder(PayloadDecoder.DEFAULT);
    }

    /**
     * @return {@link ReactorResourceFactory}
     */
    @Bean
    public ReactorResourceFactory reactorResourceFactory()
    {
        ReactorResourceFactory factory = new ReactorResourceFactory();
        factory.setUseGlobalResources(false);
        factory.setLoopResources(LoopResources.create("server", 4, true));

        return factory;
    }
}
