// Created: 02.09.2021
package de.freese.spring.rsocket.client;

import org.springframework.boot.autoconfigure.rsocket.RSocketRequesterAutoConfiguration;
import org.springframework.boot.autoconfigure.rsocket.RSocketStrategiesAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.client.reactive.ReactorResourceFactory;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.messaging.rsocket.RSocketRequester.Builder;
import org.springframework.messaging.rsocket.RSocketStrategies;
import org.springframework.stereotype.Component;

import reactor.netty.resources.LoopResources;

/**
 * @author Thomas Freese
 *
 * @see RSocketRequesterAutoConfiguration
 * @see RSocketStrategiesAutoConfiguration
 */
@Component
public class RSocketClientConfig
{
    /**
     * @return {@link ReactorResourceFactory}
     */
    @Bean
    public ReactorResourceFactory reactorResourceFactory()
    {
        ReactorResourceFactory factory = new ReactorResourceFactory();
        factory.setUseGlobalResources(false);
        factory.setLoopResources(LoopResources.create("client", 4, true));

        return factory;
    }

    /**
     * @param strategies {@link org.springframework.messaging.rsocket.RSocketStrategies}
     *
     * @return {@link Builder}
     */
    @Bean
    public RSocketRequester.Builder rSocketRequesterBuilder(final RSocketStrategies strategies)
    {
        RSocketRequester.Builder builder = RSocketRequester.builder().rsocketStrategies(strategies);

        return builder;
    }
}
