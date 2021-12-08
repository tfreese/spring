// Created: 28.10.2018
package de.freese.spring.jwt.token;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

/**
 * @author Thomas Freese
 */
public class JwtTokenUtils
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
     * Erstellt ein neues {@link JwtTokenUtils} Object.
     *
     * @param base64EncodedSecretKey String: Base64.getEncoder().encodeToString(secretKey.getBytes(StandardCharsets.UTF_8))
     * @param validityInMilliseconds long
     */
    public JwtTokenUtils(final String base64EncodedSecretKey, final long validityInMilliseconds)
    {
        super();

        this.base64EncodedSecretKey = base64EncodedSecretKey;
        this.validityInMilliseconds = validityInMilliseconds;
    }

    /**
     * @param username String
     *
     * @return String
     */
    public String createToken(final String username)
    {
        return createToken(username, null);
    }

    /**
     * @param username String
     * @param password String
     *
     * @return String
     */
    public String createToken(final String username, final String password)
    {
        return createToken(username, password, Collections.emptySet());
    }

    /**
     * @param username String
     * @param password String
     * @param roles {@link Collection}
     *
     * @return String
     */
    public String createToken(final String username, final String password, final Collection<? extends GrantedAuthority> roles)
    {
        Claims claims = Jwts.claims().setSubject(username);

        if ((password != null) && !password.isBlank())
        {
            claims.put("password", password);
        }

        if (roles != null)
        {
            // @formatter:off
            String rolesString = roles.stream()
                    .filter(Objects::nonNull)
                    .map(GrantedAuthority::getAuthority)
                    .distinct()
                    .sorted()
                    .collect(Collectors.joining(","))
                    ;
            // @formatter:on

            claims.put("roles", rolesString);
        }

        Date now = new Date();
        Date validity = new Date(now.getTime() + this.validityInMilliseconds);

        // @formatter:off
        return Jwts.builder()
                .setClaims(claims)
                .setIssuer("tommy")
                .setIssuedAt(now)
                .setExpiration(validity)
//                .compressWith(CompressionCodecs.DEFLATE)
                .signWith(SignatureAlgorithm.HS512, this.base64EncodedSecretKey)
                .compact();
        // @formatter:on
    }

    /**
     * @param claims {@link Jws}
     *
     * @return {@link Date}
     */
    public Date getExpirationDate(final Jws<Claims> claims)
    {
        return claims.getBody().getExpiration();
    }

    /**
     * @param claims {@link Jws}
     *
     * @return String
     */
    public String getPassword(final Jws<Claims> claims)
    {
        return (String) claims.getBody().get("password");
    }

    /**
     * @param claims {@link Jws}
     *
     * @return {@link Set}
     */
    public Set<GrantedAuthority> getRoles(final Jws<Claims> claims)
    {
        String rolesString = (String) claims.getBody().get("roles");

        if ((rolesString == null) || rolesString.isBlank())
        {
            return Collections.emptySet();
        }

        String[] rolesArray = rolesString.split(",");

        return Arrays.stream(rolesArray).map(SimpleGrantedAuthority::new).collect(Collectors.toSet());
    }

    /**
     * @param claims {@link Jws}
     *
     * @return String
     */
    public String getUsername(final Jws<Claims> claims)
    {
        return claims.getBody().getSubject();
    }

    /**
     * @param token String
     *
     * @return String
     */
    public String getUsernameFromToken(final String token)
    {
        Jws<Claims> claims = parseToken(token);

        return getUsername(claims);
    }

    /**
     * @param claims {@link Jws}
     *
     * @return boolean
     */
    public boolean isTokenExpired(final Jws<Claims> claims)
    {
        final Date expiration = getExpirationDate(claims);

        return expiration.before(new Date());
    }

    /**
     * @param token String
     *
     * @return boolean
     */
    public Jws<Claims> parseToken(final String token)
    {
        // @formatter:off
        return Jwts.parser()
                .setSigningKey(this.base64EncodedSecretKey)
                .parseClaimsJws(token)
                ;
        // @formatter:on
    }

    /**
     * @param req {@link HttpServletRequest}
     *
     * @return String
     */
    public String resolveToken(final HttpServletRequest req)
    {
        String bearerToken = req.getHeader("Authorization");

        if ((bearerToken != null) && bearerToken.startsWith("Bearer "))
        {
            return bearerToken.substring(7);
        }

        return null;
    }

    /**
     * @param claims {@link Jws}
     * @param userDetails {@link UserDetails}
     *
     * @return boolean
     */
    public boolean validateToken(final Jws<Claims> claims, final UserDetails userDetails)
    {
        final String username = getUsername(claims);

        return (username.equals(userDetails.getUsername()) && !isTokenExpired(claims));
    }
}
