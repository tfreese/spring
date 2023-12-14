// Created: 17.02.2022
package de.freese.spring.rsocket.config.server;

import java.util.HashMap;
import java.util.Map;

import io.rsocket.core.Resume;
import io.rsocket.frame.decoder.PayloadDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.rsocket.server.RSocketServerCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.messaging.rsocket.RSocketStrategies;
import org.springframework.messaging.rsocket.annotation.support.RSocketMessageHandler;
import org.springframework.security.core.userdetails.MapReactiveUserDetailsService;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.password.Pbkdf2PasswordEncoder;
import org.springframework.security.crypto.password.Pbkdf2PasswordEncoder.SecretKeyFactoryAlgorithm;
import org.springframework.security.messaging.handler.invocation.reactive.AuthenticationPrincipalArgumentResolver;

/**
 * @author Thomas Freese
 */
abstract class AbstractServerConfig {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Bean
    ReactiveUserDetailsService authorization(final PasswordEncoder passwordEncoder) {
        final UserDetails user = User.builder().passwordEncoder(passwordEncoder::encode).username("user").password("pass").roles("USER").build();
        final UserDetails admin = User.builder().passwordEncoder(passwordEncoder::encode).username("fail").password("pass").roles("NONE").build();

        return new MapReactiveUserDetailsService(user, admin);
    }

    @Bean
    RSocketServerCustomizer customizeRSocketServer() {
        getLogger().info("customizeRSocketServer");

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

    @Bean
    RSocketMessageHandler messageHandler(final RSocketStrategies rSocketStrategies) {
        final RSocketMessageHandler handler = new RSocketMessageHandler();
        handler.setRSocketStrategies(rSocketStrategies);

        // Wird für Login/Security benötigt.
        handler.getArgumentResolverConfigurer().addCustomResolver(new AuthenticationPrincipalArgumentResolver());

        return handler;
    }

    // @Bean
    // RSocketServerFactory rSocketServerFactory()
    // {
    // // TODO RSocketServer manuell erstellen und konfigurieren.
    //        return socketAcceptor -> null;
    // }

    @Bean
    PasswordEncoder passwordEncoder() {
        final Pbkdf2PasswordEncoder pbkdf2passwordEncoder = new Pbkdf2PasswordEncoder("mySecret", 16, 310000, SecretKeyFactoryAlgorithm.PBKDF2WithHmacSHA512);
        pbkdf2passwordEncoder.setEncodeHashAsBase64(false);

        final Map<String, PasswordEncoder> encoders = new HashMap<>();
        encoders.put("bcrypt", new BCryptPasswordEncoder(10));
        encoders.put("pbkdf2", pbkdf2passwordEncoder);
        encoders.put("noop", new PasswordEncoder() {
            @Override
            public String encode(final CharSequence rawPassword) {
                return rawPassword.toString();
            }

            @Override
            public boolean matches(final CharSequence rawPassword, final String encodedPassword) {
                return rawPassword.toString().equals(encodedPassword);
            }
        });

        final DelegatingPasswordEncoder passwordEncoder = new DelegatingPasswordEncoder("noop", encoders);
        // passwordEncoder.setDefaultPasswordEncoderForMatches(NoOpPasswordEncoder.getInstance());

        return passwordEncoder;
    }

    protected Logger getLogger() {
        return this.logger;
    }

    // @Bean
    // ReactorResourceFactory reactorResourceFactory()
    // {
    // LOGGER.info("reactorResourceFactory");
    //
    // final ReactorResourceFactory factory = new ReactorResourceFactory();
    // factory.setUseGlobalResources(false);
    // factory.setLoopResources(LoopResources.create("server", 4, true));
    //
    // return factory;
    // }

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
    // */
    // @Bean
    // RSocketStrategies rSocketStrategies()
    // {
    // LOGGER.info("rSocketStrategies");
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
