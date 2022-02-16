// Created: 12.03.2020
package de.freese.spring.rsocket;

import java.util.HashMap;
import java.util.Map;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.rsocket.RSocketStrategies;
import org.springframework.messaging.rsocket.annotation.support.RSocketMessageHandler;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.rsocket.EnableRSocketSecurity;
import org.springframework.security.config.annotation.rsocket.RSocketSecurity;
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
import org.springframework.security.rsocket.core.PayloadSocketAcceptorInterceptor;

/**
 * @author Thomas Freese
 */
@Configuration
@EnableRSocketSecurity
@EnableReactiveMethodSecurity
public class RSocketServerSecurityConfig
{
    /**
     * @param security {@link RSocketSecurity}
     *
     * @return {@link PayloadSocketAcceptorInterceptor}
     */
    @Bean
    PayloadSocketAcceptorInterceptor authentication(final RSocketSecurity security)
    {
        //@formatter:off
        security.authorizePayload(authorize ->
            authorize
                    // User muss ROLE_SETUP haben um Verbindung zum Server herzustellen.
                    //.setup().hasRole("SETUP")
                    // User muss ROLE_ADMIN haben für das Absetzen der Requests auf die End-Punkte.
                    //.route("greet/*").hasRole("ADMIN")
                    //.anyRequest().authenticated();
                    .anyExchange().authenticated()
        )
        .simpleAuthentication(Customizer.withDefaults())
        ;
        //@formatter:on

        return security.build();
    }

    /**
     * @param passwordEncoder {@link PasswordEncoder}
     *
     * @return {@link ReactiveUserDetailsService}
     */
    @Bean
    ReactiveUserDetailsService authorization(final PasswordEncoder passwordEncoder)
    {
        UserDetails user = User.builder().username("user").password(passwordEncoder.encode("pass")).roles("USER").build();

        UserDetails admin = User.builder().username("fail").password(passwordEncoder.encode("pass")).roles("NONE").build();

        return new MapReactiveUserDetailsService(user, admin);
    }

    /**
     * @param rSocketStrategies {@link RSocketStrategies}
     *
     * @return {@link RSocketMessageHandler}
     */
    @Bean
    RSocketMessageHandler messageHandler(final RSocketStrategies rSocketStrategies)
    {
        RSocketMessageHandler handler = new RSocketMessageHandler();
        handler.setRSocketStrategies(rSocketStrategies);

        // Wird für Login/Security benötigt.
        handler.getArgumentResolverConfigurer().addCustomResolver(new AuthenticationPrincipalArgumentResolver());

        return handler;
    }

    /**
     * @return {@link PasswordEncoder}
     */
    @Bean
    PasswordEncoder passwordEncoder()
    {
        Pbkdf2PasswordEncoder pbkdf2passwordEncoder = new Pbkdf2PasswordEncoder("mySecret");
        pbkdf2passwordEncoder.setAlgorithm(SecretKeyFactoryAlgorithm.PBKDF2WithHmacSHA512);
        pbkdf2passwordEncoder.setEncodeHashAsBase64(false);

        Map<String, PasswordEncoder> encoders = new HashMap<>();
        encoders.put("BCRYPT", new BCryptPasswordEncoder(10));
        encoders.put("PBKDF2", pbkdf2passwordEncoder);
        encoders.put("PLAIN", new PasswordEncoder()
        {
            @Override
            public String encode(final CharSequence rawPassword)
            {
                return rawPassword.toString();
            }

            @Override
            public boolean matches(final CharSequence rawPassword, final String encodedPassword)
            {
                return rawPassword.toString().equals(encodedPassword);
            }
        });

        DelegatingPasswordEncoder passwordEncoder = new DelegatingPasswordEncoder("PLAIN", encoders);
        // passwordEncoder.setDefaultPasswordEncoderForMatches(NoOpPasswordEncoder.getInstance());

        return passwordEncoder;
    }
}
