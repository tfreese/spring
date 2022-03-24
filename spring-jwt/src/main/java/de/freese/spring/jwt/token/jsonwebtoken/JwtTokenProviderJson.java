// Created: 22.12.2021
package de.freese.spring.jwt.token.jsonwebtoken;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Date;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import de.freese.spring.jwt.token.JwtToken;
import de.freese.spring.jwt.token.JwtTokenProvider;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwt;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.impl.DefaultHeader;
import io.jsonwebtoken.impl.DefaultJwt;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;

/**
 * @author Thomas Freese
 */
public class JwtTokenProviderJson implements JwtTokenProvider
{
    /**
     *
     */
    private final String base64EncodedSecretKey;
    /**
     *
     */
    private final long validityInMilliseconds;

    /**
     * Erstellt ein neues {@link JwtTokenProviderJson} Object.
     *
     * @param secretKey String
     * @param validityInMilliseconds long
     */
    public JwtTokenProviderJson(final String secretKey, final long validityInMilliseconds)
    {
        super();

        this.base64EncodedSecretKey = Base64.getEncoder().encodeToString(secretKey.getBytes(StandardCharsets.UTF_8));
        this.validityInMilliseconds = validityInMilliseconds;
    }

    /**
     * @see de.freese.spring.jwt.token.JwtTokenProvider#createToken(java.lang.String, java.lang.String, java.util.Set)
     */
    @SuppressWarnings("javadoc")
    @Override
    public String createToken(final String username, final String password, final Set<String> roles)
    {
        Claims claims = Jwts.claims().setSubject(username);

        if ((password != null) && !password.isBlank())
        {
            claims.put("password", password);
        }

        if ((roles != null) && !roles.isEmpty())
        {
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

        // @formatter:off
        return Jwts.builder()
                .setClaims(claims)
                .setIssuer("tommy")
                .setIssuedAt(now)
                .setExpiration(expiration)
                .setId(UUID.randomUUID().toString())
//                .compressWith(CompressionCodecs.DEFLATE)
                .signWith(SignatureAlgorithm.HS512, this.base64EncodedSecretKey)
                .compact()
                ;
        // @formatter:on;
    }

    /**
     * @see de.freese.spring.jwt.token.JwtTokenProvider#parseToken(java.lang.String)
     */
    @Override
    public JwtToken parseToken(final String token) throws AuthenticationException
    {
        try
        {
            // @formatter:off
            Jws<Claims> claims = Jwts.parser()
                    .setSigningKey(this.base64EncodedSecretKey)
                    .parseClaimsJws(token)
                    ;
            // @formatter:on

            Jwt<?, Jws<Claims>> jwt = new DefaultJwt<>(new DefaultHeader<>(), claims);

            return new JwtTokenJson(jwt);
        }
        catch (IllegalArgumentException ex)
        {
            throw new AuthenticationServiceException("Unable to get JWT Token");
        }
        catch (ExpiredJwtException ex)
        {
            throw new AuthenticationServiceException("JwtToken is expired");
        }
        catch (SignatureException ex)
        {
            throw new BadCredentialsException("Authentication Failed. Username or Password not valid");
        }
        catch (JwtException ex)
        {
            throw new AuthenticationServiceException("JwtToken is invalid");
        }
    }
}
