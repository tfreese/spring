// Created: 11.03.2020
package de.freese.spring.rsocket.config;

import io.rsocket.core.Resume;
import io.rsocket.frame.decoder.PayloadDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.rsocket.RSocketServerAutoConfiguration;
import org.springframework.boot.rsocket.netty.NettyRSocketServerFactory;
import org.springframework.boot.rsocket.server.RSocketServerCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

/**
 * @author Thomas Freese
 *
 * @see NettyRSocketServerFactory
 * @see RSocketServerAutoConfiguration
 */
@Component
public class ServerConfig
{
    /**
     *
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(ServerConfig.class);

    /**
     * @return {@link RSocketServerCustomizer}
     */
    @Bean
    RSocketServerCustomizer customizeRSocketServer()
    {
        LOGGER.info("customizeRSocketServer");

        // @formatter:off
        return rSocketServer -> rSocketServer
                // Make the socket capable of resumption.
                // By default, the Resume Session will have a duration of 120s,
                // a timeout of 10s, and use the In Memory (volatile, non-persistent) session store.
                .resume(new Resume())
                .payloadDecoder(PayloadDecoder.ZERO_COPY)
                .fragment(1492)
                ;
        // @formatter:on
    }

    // /**
    // * @return {@link RSocketServerFactory}
    // */
    // @Bean
    // RSocketServerFactory rSocketServerFactory()
    // {
    // // TODO RSocketServer manuell erstellen und konfigurieren.
//        // @formatter:off
//        return socketAcceptor -> null;
//        // @formatter:on
    // }

    // /**
    // * @return {@link ReactorResourceFactory}
    // */
    // @Bean
    // ReactorResourceFactory reactorResourceFactory()
    // {
    // LOGGER.info("reactorResourceFactory");
    //
    // ReactorResourceFactory factory = new ReactorResourceFactory();
    // factory.setUseGlobalResources(false);
    // factory.setLoopResources(LoopResources.create("server", 4, true));
    //
    // return factory;
    // }

    // /**
    // * @return {@link RSocketStrategiesCustomizer}
    // */
    // @Bean
    // RSocketStrategiesCustomizer rSocketStrategiesCustomizer()
    // {
//        // @formatter:off
//        return strategies ->
//            //strategies.dataBufferFactory(null)
//        ;
//        // @formatter:on
    // }

    // /**
    // * Optional
    // *
    // * @return {@link RSocketStrategies}
    // */
    // @Bean
    // RSocketStrategies rsocketStrategies()
    // {
    // LOGGER.info("rsocketStrategies");
    //
//        // @formatter:off
//        return RSocketStrategies.builder()
////                .decoder(new Jackson2JsonDecoder())
////                .encoder(new Jackson2JsonEncoder())
////                .decoder(new Jackson2CborDecoder())
////                .encoder(new Jackson2CborEncoder())
////                .dataBufferFactory(new DefaultDataBufferFactory(true))
//                // .routeMatcher(new PathPatternRouteMatcher() // Nur für Client
//                .build()
//                ;
//        // @formatter:on
    // }
}
