// Created: 22.12.2021
package de.freese.spring.jwt.token.jsonwebtoken;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collections;
import java.util.Date;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ClaimsBuilder;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwt;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.impl.DefaultHeader;
import io.jsonwebtoken.impl.DefaultJwt;
import io.jsonwebtoken.security.SecurityException;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;

import de.freese.spring.jwt.token.JwtToken;
import de.freese.spring.jwt.token.JwtTokenProvider;

/**
 * @author Thomas Freese
 */
public class JwtTokenProviderJson implements JwtTokenProvider {
    private final SecretKey secretKey;
    private final long validityInMilliseconds;

    public JwtTokenProviderJson(final long validityInMilliseconds, final String secretKey) {
        super();

        this.validityInMilliseconds = validityInMilliseconds;

        final String base64EncodedSecretKey = Base64.getEncoder().encodeToString(secretKey.repeat(4).getBytes(StandardCharsets.UTF_8));
        final byte[] decodedKey = Base64.getDecoder().decode(base64EncodedSecretKey);

        try {
            //        Jwts.SIG.HS256
            //            final SecretKey secretKey = Keys.hmacShaKeyFor(decodedKey);
            final Mac mac = Mac.getInstance("HmacSHA256");
            this.secretKey = new SecretKeySpec(decodedKey, 0, decodedKey.length, mac.getAlgorithm());
        }
        catch (RuntimeException ex) {
            throw ex;
        }
        catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public JwtTokenProviderJson(final long validityInMilliseconds, final SecretKey secretKey) {
        super();

        this.validityInMilliseconds = validityInMilliseconds;
        this.secretKey = secretKey;
    }

    @Override
    public String createToken(final String username, final String password, final Set<String> roles) {
        final ClaimsBuilder claimsBuilder = Jwts.claims().subject(username);

        if ((password != null) && !password.isBlank()) {
            claimsBuilder.add("password", password);
        }

        if ((roles != null) && !roles.isEmpty()) {
            // @formatter:off
            final String rolesString = roles.stream()
                    .filter(Objects::nonNull)
                    .distinct()
                    .sorted()
                    .collect(Collectors.joining(","))
                    ;
            // @formatter:on

            claimsBuilder.add("roles", rolesString);
        }

        final Date now = new Date();
        final Date expiration = new Date(now.getTime() + this.validityInMilliseconds);

        // @formatter:off
        return Jwts.builder()
                .claims(claimsBuilder.build())
                .issuer("tommy")
                .issuedAt(now)
                .expiration(expiration)
                .id(UUID.randomUUID().toString())
//                .compressWith(CompressionCodecs.DEFLATE)
                .signWith(secretKey) // Jwts.SIG.HS256
                .compact()
                ;
        // @formatter:on;
    }

    @Override
    public JwtToken parseToken(final String token) throws AuthenticationException {
        try {
            // @formatter:off
            final Jws<Claims> claims = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    ;
            // @formatter:on

            final Jwt<?, Jws<Claims>> jwt = new DefaultJwt<>(new DefaultHeader(Collections.emptyMap()), claims);

            return new JwtTokenJson(jwt);
        }
        catch (IllegalArgumentException ex) {
            throw new AuthenticationServiceException("Unable to get JWT Token");
        }
        catch (ExpiredJwtException ex) {
            throw new AuthenticationServiceException("JwtToken is expired");
        }
        catch (SecurityException ex) {
            throw new BadCredentialsException("Authentication Failed. Username or Password not valid");
        }
        catch (JwtException ex) {
            throw new AuthenticationServiceException("JwtToken is invalid");
        }
    }
}
