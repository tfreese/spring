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

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import io.jsonwebtoken.Claims;
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
    private final String base64EncodedSecretKey;

    private final long validityInMilliseconds;

    public JwtTokenProviderJson(final String secretKey, final long validityInMilliseconds) {
        super();

        this.base64EncodedSecretKey = Base64.getEncoder().encodeToString(secretKey.getBytes(StandardCharsets.UTF_8));
        this.validityInMilliseconds = validityInMilliseconds;
    }

    @Override
    public String createToken(final String username, final String password, final Set<String> roles) {
        Claims claims = Jwts.claims().subject(username).build();

        if ((password != null) && !password.isBlank()) {
            claims.put("password", password);
        }

        if ((roles != null) && !roles.isEmpty()) {
            // @formatter:off
            String rolesString = roles.stream()
                    .filter(Objects::nonNull)
                    .distinct()
                    .sorted()
                    .collect(Collectors.joining(","))
                    ;
            // @formatter:on

            claims.put("roles", rolesString);
        }

        Date now = new Date();
        Date expiration = new Date(now.getTime() + this.validityInMilliseconds);

        SecretKey secretKey = new SecretKeySpec(Base64.getDecoder().decode(this.base64EncodedSecretKey), Jwts.SIG.HS512.getId());

        // @formatter:off
        return Jwts.builder()
                .claims(claims)
                .issuer("tommy")
                .issuedAt(now)
                .expiration(expiration)
                .id(UUID.randomUUID().toString())
//                .compressWith(CompressionCodecs.DEFLATE)
                .signWith(secretKey)
                .compact()
                ;
        // @formatter:on;
    }

    @Override
    public JwtToken parseToken(final String token) throws AuthenticationException {
        try {
            SecretKey secretKey = new SecretKeySpec(Base64.getDecoder().decode(this.base64EncodedSecretKey), Jwts.SIG.HS512.getId());

            // @formatter:off
            Jws<Claims> claims = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    ;
            // @formatter:on

            Jwt<?, Jws<Claims>> jwt = new DefaultJwt<>(new DefaultHeader(Collections.emptyMap()), claims);

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
