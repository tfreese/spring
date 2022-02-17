// Created: 12.03.2020
package de.freese.spring.rsocket.config;

import java.text.ParseException;
import java.time.Instant;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWEDecrypter;
import com.nimbusds.jose.crypto.PasswordBasedDecrypter;
import com.nimbusds.jwt.EncryptedJWT;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.AccountExpiredException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.rsocket.EnableRSocketSecurity;
import org.springframework.security.config.annotation.rsocket.RSocketSecurity;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtReactiveAuthenticationManager;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverterAdapter;
import org.springframework.security.rsocket.core.PayloadSocketAcceptorInterceptor;
import org.springframework.security.web.authentication.www.NonceExpiredException;
import reactor.core.publisher.Mono;

/**
 * @author Thomas Freese
 */
@Configuration
@EnableRSocketSecurity
@EnableReactiveMethodSecurity
@Profile("jwt")
public class JwtServerSecurityConfig extends AbstractServerSecurityConfig
{
    /**
     * @param security {@link RSocketSecurity}
     * @param reactiveAuthenticationManager {@link ReactiveAuthenticationManager}
     *
     * @return {@link PayloadSocketAcceptorInterceptor}
     */
    @Bean
    PayloadSocketAcceptorInterceptor authentication(final RSocketSecurity security, final ReactiveAuthenticationManager reactiveAuthenticationManager)
    {
        //@formatter:off
        security.authorizePayload(authorize ->
            authorize
                    // User muss ROLE_SETUP haben um Verbindung zum Server herzustellen.
                    //.setup().hasRole("SETUP")
                    // User muss ROLE_ADMIN haben für das Absetzen der Requests auf die End-Punkte.
                    //.route("greet/*").hasRole("ADMIN")
                    .anyRequest().authenticated()
                    .anyExchange().authenticated()
        )
        .jwt(jwtSpec -> jwtSpec.authenticationManager(reactiveAuthenticationManager))
        ;
        //@formatter:on

        return security.build();
    }

    /**
     * @param reactiveUserDetailsService {@link ReactiveUserDetailsService}
     * @param passwordEncoder {@link PasswordEncoder}
     *
     * @return {@link JwtAuthenticationConverter}
     */
    @Bean
    Converter<Jwt, AbstractAuthenticationToken> authenticationConverter(final ReactiveUserDetailsService reactiveUserDetailsService,
                                                                        final PasswordEncoder passwordEncoder)
    {
        // JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
        // jwtGrantedAuthoritiesConverter.setAuthorityPrefix("ROLE_");
        //
        // // TODO Eigenen AuthenticationConverter um aus dem Token die UserDetails zu laden und ein JwtAuthenticationToken zu erstellen.
        // JwtAuthenticationConverter authenticationConverter = new JwtAuthenticationConverter(); // TODO
        // authenticationConverter.setJwtGrantedAuthoritiesConverter(jwtGrantedAuthoritiesConverter);
        //
        // return authenticationConverter;

        return jwt -> {
            String user = jwt.getSubject();
            String password = jwt.getClaimAsString("password");
            Instant expiresAt = jwt.getExpiresAt();

            UserDetails userDetails = reactiveUserDetailsService.findByUsername(user).block();

            if (userDetails == null)
            {
                ReactiveSecurityContextHolder.clearContext();
                throw new BadCredentialsException("Bad credentials");
            }

            if (!userDetails.isAccountNonLocked())
            {
                throw new LockedException("User account is locked");
            }

            if (!userDetails.isEnabled())
            {
                throw new DisabledException("User is disabled");
            }

            if (!userDetails.isAccountNonExpired())
            {
                throw new AccountExpiredException("User account has expired");
            }

            if (!userDetails.isCredentialsNonExpired())
            {
                throw new CredentialsExpiredException("User credentials have expired");
            }

            if ((userDetails.getPassword() == null) || !passwordEncoder.matches(password, userDetails.getPassword()))
            {
                throw new BadCredentialsException("Bad credentials");
            }

            if (Instant.now().isAfter(expiresAt))
            {
                throw new NonceExpiredException("Token has expired");
            }

            return new JwtAuthenticationToken(jwt, userDetails.getAuthorities(), user);
        };
    }

    /**
     * @param reactiveJwtDecoder {@link ReactiveJwtDecoder}
     * @param authenticationConverter {@link Converter}
     *
     * @return {@link JwtReactiveAuthenticationManager}
     */
    @Bean
    JwtReactiveAuthenticationManager jwtReactiveAuthenticationManager(final ReactiveJwtDecoder reactiveJwtDecoder,
                                                                      final Converter<Jwt, AbstractAuthenticationToken> authenticationConverter)
    {
        JwtReactiveAuthenticationManager jwtReactiveAuthenticationManager = new JwtReactiveAuthenticationManager(reactiveJwtDecoder);
        jwtReactiveAuthenticationManager.setJwtAuthenticationConverter(new ReactiveJwtAuthenticationConverterAdapter(authenticationConverter));

        return jwtReactiveAuthenticationManager;
    }

    /**
     * @return {@link ReactiveJwtDecoder}
     *
     * @throws Exception Falls was schief geht.
     */
    @Bean
    ReactiveJwtDecoder reactiveJwtDecoder() throws Exception
    {
        // Mac mac = Mac.getInstance("HmacSHA256");
        // SecretKeySpec secretKey = new SecretKeySpec("my-secret".getBytes(), mac.getAlgorithm());
        //
//        // @formatter:off
//        return NimbusReactiveJwtDecoder.withSecretKey(secretKey)
//                .macAlgorithm(MacAlgorithm.HS256)
//                .build()
//                ;
//        // @formatter:on
        return token -> {
            try
            {
                EncryptedJWT jwt = EncryptedJWT.parse(token);
                JWEDecrypter decrypter = new PasswordBasedDecrypter("my-password");
                jwt.decrypt(decrypter);

                // JWT jwt = PlainJWT.parse(token);

                // @formatter:off
                Jwt jwtSpring = Jwt.withTokenValue(token)
                        .subject(jwt.getJWTClaimsSet().getSubject())
                        .claim("password", jwt.getJWTClaimsSet().getClaim("password"))
                        .expiresAt(jwt.getJWTClaimsSet().getExpirationTime().toInstant())
                        .build()
                        ;
                // @formatter:on

                return Mono.just(jwtSpring);
            }
            catch (ParseException | JOSEException ex)
            {
                throw new RuntimeException(ex);
            }
        };
    }
}
