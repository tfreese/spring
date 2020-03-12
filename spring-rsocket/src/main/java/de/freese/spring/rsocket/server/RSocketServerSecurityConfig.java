/**
 * Created: 12.03.2020
 */

package de.freese.spring.rsocket.server;

import java.util.HashMap;
import java.util.Map;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.http.codec.json.Jackson2JsonEncoder;
import org.springframework.messaging.rsocket.RSocketStrategies;
import org.springframework.messaging.rsocket.annotation.support.RSocketMessageHandler;
import org.springframework.security.config.Customizer;
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
import org.springframework.security.rsocket.core.PayloadSocketAcceptorInterceptor;
import org.springframework.security.rsocket.metadata.BasicAuthenticationDecoder;

/**
 * @author Thomas Freese
 */
@Configuration
@EnableRSocketSecurity
@Profile("server")
public class RSocketServerSecurityConfig
{
    /**
     * Erstellt ein neues {@link RSocketServerSecurityConfig} Object.
     */
    public RSocketServerSecurityConfig()
    {
        super();
    }

    /**
     * @param rSocketStrategies {@link RSocketStrategies}
     * @return {@link RSocketMessageHandler}
     */
    @Bean
    public RSocketMessageHandler messageHandler(final RSocketStrategies rSocketStrategies)
    {
        RSocketMessageHandler handler = new RSocketMessageHandler();
        handler.setRSocketStrategies(rSocketStrategies);

        return handler;
    }

    /**
     * @return {@link PasswordEncoder}
     */
    @Bean
    public PasswordEncoder passwordEncoder()
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

    /**
     * @param rsocket {@link RSocketSecurity}
     * @return {@link PayloadSocketAcceptorInterceptor}
     */
    @Bean
    public PayloadSocketAcceptorInterceptor rsocketInterceptor(final RSocketSecurity rsocket)
    {
        //@formatter:off
        rsocket.authorizePayload(authorize -> {
            authorize
                    // User muss ROLE_SETUP haben um Verbindung zum Server herzustellen.
                    .setup().hasRole("SETUP")
                    // User muss ROLE_ADMIN haben für das Absetzen der Requests auf die End-Punkte.
//                    .route("channel/*").hasRole("ADMIN")
//                    .route("error").hasRole("ADMIN")
//                    .route("fire-and-forget").hasRole("ADMIN")
//                    .route("request-response").hasRole("ADMIN")
//                    .route("stream").hasRole("ADMIN")
                    .route("*").hasRole("ADMIN")
                    .anyRequest().authenticated();
        }).basicAuthentication(Customizer.withDefaults())
        ;
        //@formatter:on

        return rsocket.build();
    }

    /**
     * @return {@link RSocketStrategies}
     */
    @Bean
    public RSocketStrategies rsocketStrategies()
    {
        //@formatter:off
        RSocketStrategies rSocketStrategies = RSocketStrategies.builder()
                .decoder(new BasicAuthenticationDecoder(), new Jackson2JsonDecoder())
                .encoder(new Jackson2JsonEncoder())
                .build()
                ;
        //@formatter:on

        return rSocketStrategies;
    }

    /**
     * @param passwordEncoder {@link PasswordEncoder}
     * @return {@link ReactiveUserDetailsService}
     */
    @Bean
    public ReactiveUserDetailsService userDetailsService(final PasswordEncoder passwordEncoder)
    {
        // User muss ROLE_SETUP haben um Verbindung zum Server herzustellen.
        UserDetails connectUser = User.builder().username("setup").password(passwordEncoder.encode("secret")).roles("SETUP").build();

        // User muss ROLE_ADMIN haben für das Absetzen der Requests auf die End-Punkte.
        UserDetails adminUser = User.builder().username("tommy").password(passwordEncoder.encode("gehaim")).roles("ADMIN").build();

        return new MapReactiveUserDetailsService(connectUser, adminUser);
    }
}
