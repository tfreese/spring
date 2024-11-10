// Created: 12.03.2020
package de.freese.spring.rsocket.config.server;

import java.text.ParseException;
import java.time.Instant;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWEDecrypter;
import com.nimbusds.jose.crypto.PasswordBasedDecrypter;
import com.nimbusds.jwt.EncryptedJWT;
import com.nimbusds.jwt.JWTClaimsSet;
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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.rsocket.EnableRSocketSecurity;
import org.springframework.security.config.annotation.rsocket.RSocketSecurity;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.MappedJwtClaimSetConverter;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
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
public class JwtAuthServerConfig extends AbstractServerConfig {
    @Bean
    PayloadSocketAcceptorInterceptor authentication(final RSocketSecurity security, final ReactiveAuthenticationManager reactiveAuthenticationManager) {
        security.authorizePayload(authorize ->
                        authorize
                                // User muss ROLE_SETUP haben, um Verbindung zum Server herzustellen.
                                //.setup().hasRole("SETUP")
                                // User muss ROLE_ADMIN haben für das Absetzen der Requests auf die End-Punkte.
                                //.route("greet/*").hasRole("ADMIN")
                                //.route("greet/*").authenticated()
                                .anyRequest().authenticated()
                                .anyExchange().authenticated()
                )
                .jwt(jwtSpec -> jwtSpec.authenticationManager(reactiveAuthenticationManager))
        ;

        return security.build();
    }

    @Bean
    Converter<Jwt, AbstractAuthenticationToken> authenticationConverter(final ReactiveUserDetailsService reactiveUserDetailsService, final PasswordEncoder passwordEncoder) {
        // final JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
        // jwtGrantedAuthoritiesConverter.setAuthorityPrefix("ROLE_");
        //
        // // TODO Eigener AuthenticationConverter um aus dem Token die UserDetails zu laden und ein JwtAuthenticationToken zu erstellen.
        // final JwtAuthenticationConverter authenticationConverter = new JwtAuthenticationConverter();
        // authenticationConverter.setJwtGrantedAuthoritiesConverter(jwtGrantedAuthoritiesConverter);
        //
        // return authenticationConverter;

        return jwt -> {
            final String user = jwt.getSubject();
            final String password = jwt.getClaimAsString("password");
            final Instant expiresAt = jwt.getExpiresAt();

            final UserDetails userDetails = reactiveUserDetailsService.findByUsername(user).block();

            if (userDetails == null) {
                ReactiveSecurityContextHolder.clearContext();
                throw new BadCredentialsException("Bad credentials");
            }

            if (!userDetails.isAccountNonLocked()) {
                throw new LockedException("User account is locked");
            }

            if (!userDetails.isEnabled()) {
                throw new DisabledException("User is disabled");
            }

            if (!userDetails.isAccountNonExpired()) {
                throw new AccountExpiredException("User account has expired");
            }

            if (!userDetails.isCredentialsNonExpired()) {
                throw new CredentialsExpiredException("User credentials have expired");
            }

            if (userDetails.getPassword() == null || !passwordEncoder.matches(password, userDetails.getPassword())) {
                throw new BadCredentialsException("Bad credentials");
            }

            if (Instant.now().isAfter(expiresAt)) {
                throw new NonceExpiredException("Token has expired");
            }

            // @AuthenticationPrincipal final org.springframework.security.oauth2.jwt.Jwt jwt
            // return new JwtAuthenticationToken(jwt, userDetails.getAuthorities(), user);

            // @AuthenticationPrincipal final UserDetails user
            return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        };
    }

    @Bean
    JwtReactiveAuthenticationManager jwtReactiveAuthenticationManager(final ReactiveJwtDecoder reactiveJwtDecoder,
                                                                      final Converter<Jwt, AbstractAuthenticationToken> authenticationConverter) {
        final JwtReactiveAuthenticationManager jwtReactiveAuthenticationManager = new JwtReactiveAuthenticationManager(reactiveJwtDecoder);
        jwtReactiveAuthenticationManager.setJwtAuthenticationConverter(new ReactiveJwtAuthenticationConverterAdapter(authenticationConverter));

        return jwtReactiveAuthenticationManager;
    }

    @Bean
    ReactiveJwtDecoder reactiveJwtDecoder() {
        // final Mac mac = Mac.getInstance("HmacSHA256");
        // final SecretKeySpec secretKey = new SecretKeySpec("my-secret".getBytes(), mac.getAlgorithm());
        //
        // return NimbusReactiveJwtDecoder.withSecretKey(secretKey)
        //         .macAlgorithm(MacAlgorithm.HS256)
        //         .build()
        //         ;

        final Converter<Map<String, Object>, Map<String, Object>> claimSetConverter = MappedJwtClaimSetConverter.withDefaults(Collections.emptyMap());

        return token -> {
            try {
                final EncryptedJWT jwt = EncryptedJWT.parse(token);
                final JWEDecrypter decrypter = new PasswordBasedDecrypter("my-password");
                jwt.decrypt(decrypter);

                // final JWT jwt = PlainJWT.parse(token);

                final Map<String, Object> headers = new LinkedHashMap<>(jwt.getHeader().toJSONObject());
                final JWTClaimsSet jwtClaimsSet = jwt.getJWTClaimsSet();
                final Map<String, Object> claims = claimSetConverter.convert(jwtClaimsSet.getClaims());

                final Jwt jwtSpring = Jwt.withTokenValue(token)
                        .headers(map -> map.putAll(headers)) // Header müssen gefüllt sein, sonst gib es Exception.
                        .claims(map -> map.putAll(claims))
                        //.issuer(jwtClaimsSet.getIssuer())
                        //.subject(jwtClaimsSet.getSubject())
                        //.claim("password", jwtClaimsSet.getClaim("password"))
                        //.expiresAt(jwtClaimsSet.getExpirationTime().toInstant())
                        //.jti(jwtClaimsSet.getJWTID())
                        .build();

                return Mono.just(jwtSpring);
            }
            catch (ParseException | JOSEException ex) {
                throw new RuntimeException(ex);
            }
        };
    }
}
