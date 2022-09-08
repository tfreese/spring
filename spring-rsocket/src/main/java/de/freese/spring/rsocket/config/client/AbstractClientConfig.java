// Created: 02.09.2021
package de.freese.spring.rsocket.config.client;

import java.time.Duration;

import io.rsocket.core.Resume;
import io.rsocket.frame.decoder.PayloadDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.rsocket.RSocketRequesterAutoConfiguration;
import org.springframework.boot.autoconfigure.rsocket.RSocketStrategiesAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.core.codec.Encoder;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.messaging.rsocket.RSocketRequester.Builder;
import org.springframework.messaging.rsocket.RSocketStrategies;
import org.springframework.util.MimeTypeUtils;
import reactor.util.retry.Retry;

/**
 * @author Thomas Freese
 * @see RSocketRequesterAutoConfiguration
 * @see RSocketStrategiesAutoConfiguration
 */
abstract class AbstractClientConfig
{
    /**
     *
     */
    private final Logger logger = LoggerFactory.getLogger(getClass());

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
    // factory.setLoopResources(LoopResources.create("client", 4, true));
    //
    // return factory;
    // }

    /**
     * @param strategies {@link org.springframework.messaging.rsocket.RSocketStrategies}
     *
     * @return {@link Builder}
     */
    @Bean
    RSocketRequester.Builder rSocketRequesterBuilder(final RSocketStrategies strategies)
    {
        getLogger().info("rSocketRequesterBuilder");

        // RSocketRequester.wrap(rSocket, dataMimeType, metaDataMimeType, strategies);

        // @formatter:off
        return RSocketRequester.builder()
                .dataMimeType(MimeTypeUtils.APPLICATION_JSON)
                .rsocketStrategies(strategies)
                .rsocketStrategies(builder ->
                    builder
                        .encoder(createAuthenticationEncoder()) // Für Security benötigt
                )
                .rsocketConnector(connector ->
                    connector
                        .payloadDecoder(PayloadDecoder.ZERO_COPY)
                        .keepAlive(Duration.ofSeconds(30), Duration.ofSeconds(60))
                        .resume(new Resume())
                        .reconnect(Retry.fixedDelay(3, Duration.ofSeconds(1)))
                        .fragment(1492)
                )
                //.transport(TcpClientTransport.create(TcpClient.create().host("localhost").port(7000).runOn(LoopResources.create("client", 4, true))))
                ;
        // @formatter:on
    }

    /**
     * @return {@link Encoder}
     */
    protected abstract Encoder<?> createAuthenticationEncoder();

    /**
     * @return {@link Logger}
     */
    protected Logger getLogger()
    {
        return this.logger;
    }

    // /**
    // * @return {@link RSocketStrategiesCustomizer}
    // */
    // @Bean
    // RSocketStrategiesCustomizer rSocketStrategiesCustomizer()
    // {
    //        // @formatter:off
//        return strategies ->
//            strategies.encoder(new SimpleAuthenticationEncoder())
//            //.dataBufferFactory(null)
//        ;
//        // @formatter:on
    // }

    // /**
    // * Optional
    // *
    // * @return {@link RSocketStrategies}
    // */
    // @Bean
    // RSocketStrategies rSocketStrategies()
    // {
    //        // @formatter:off
//        return RSocketStrategies.builder()
////                .decoder(new Jackson2JsonDecoder())
////                .encoder(new Jackson2JsonEncoder())
//                .decoder(new Jackson2CborDecoder())
//                .encoder(new Jackson2CborEncoder())
////                .dataBufferFactory(new DefaultDataBufferFactory(true))
//                // .routeMatcher(new PathPatternRouteMatcher() // Nur für Client
//                .build()
//                ;
//        // @formatter:on
    // }
}
