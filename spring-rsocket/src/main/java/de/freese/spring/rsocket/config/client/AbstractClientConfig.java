// Created: 02.09.2021
package de.freese.spring.rsocket.config.client;

import java.time.Duration;

import io.rsocket.core.Resume;
import io.rsocket.frame.decoder.PayloadDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.core.codec.Encoder;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.messaging.rsocket.RSocketStrategies;
import org.springframework.util.MimeTypeUtils;
import reactor.util.retry.Retry;

/**
 * @author Thomas Freese
 */
abstract class AbstractClientConfig<T extends Encoder<?>> {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    // @Bean
    // ReactorResourceFactory reactorResourceFactory() {
    // LOGGER.info("reactorResourceFactory");
    //
    // final ReactorResourceFactory factory = new ReactorResourceFactory();
    // factory.setUseGlobalResources(false);
    // factory.setLoopResources(LoopResources.create("client", 4, true));
    //
    // return factory;
    // }

    @Bean
    RSocketRequester.Builder rSocketRequesterBuilder(final RSocketStrategies strategies) {
        getLogger().info("rSocketRequesterBuilder");

        // RSocketRequester.wrap(rSocket, dataMimeType, metaDataMimeType, strategies);

        return RSocketRequester.builder()
                .dataMimeType(MimeTypeUtils.APPLICATION_JSON)
                .rsocketStrategies(strategies)
                .rsocketStrategies(builder ->
                        builder
                                .encoder(createAuthenticationEncoder()) // Need Security.
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
    }

    protected abstract T createAuthenticationEncoder();

    protected Logger getLogger() {
        return logger;
    }

    // @Bean
    // RSocketStrategiesCustomizer rSocketStrategiesCustomizer() {
    //    return strategies ->
    //        strategies.encoder(new SimpleAuthenticationEncoder())
    //        //.dataBufferFactory(null)
    //    ;
    // }

    // /**
    // * Optional
    // */
    // @Bean
    // RSocketStrategies rSocketStrategies() {
    //        return RSocketStrategies.builder()
    // //                .decoder(new Jackson2JsonDecoder())
    // //                .encoder(new Jackson2JsonEncoder())
    //                .decoder(new Jackson2CborDecoder())
    //                .encoder(new Jackson2CborEncoder())
    // //                .dataBufferFactory(new DefaultDataBufferFactory(true))
    //                // .routeMatcher(new PathPatternRouteMatcher()) // Only for Client.
    //                .build()
    //                ;
    // }
}
