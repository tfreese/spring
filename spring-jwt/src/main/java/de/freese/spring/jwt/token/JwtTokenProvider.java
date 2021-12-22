// Created: 22.12.2021
package de.freese.spring.jwt.token;

import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;

/**
 * @author Thomas Freese
 */
public interface JwtTokenProvider
{
    /**
     * @param username String
     * @param password String
     *
     * @return String
     */
    default String createToken(final String username, final String password)
    {
        Set<String> roles = null;

        return createToken(username, password, roles);
    }

    /**
     * @param username String
     * @param password String
     * @param roles {@link Collection}; Optional
     *
     * @return String
     */
    default String createToken(final String username, final String password, final Collection<? extends GrantedAuthority> roles)
    {
        Set<String> rolesSet = null;

        if ((roles != null) && !roles.isEmpty())
        {
            // @formatter:off
            rolesSet = roles.stream()
                    .filter(Objects::nonNull)
                    .map(GrantedAuthority::getAuthority)
                    .distinct()
                    .collect(Collectors.toSet())
                    ;
            // @formatter:on
        }

        return createToken(username, password, rolesSet);
    }

    /**
     * @param username String
     * @param password String
     * @param roles {@link Set}; Optional
     *
     * @return String
     */
    String createToken(final String username, final String password, final Set<String> roles);

    /**
     * @param token String
     *
     * @return {@link JwtToken}
     *
     * @throws AuthenticationException Falls was schief geht.
     */
    JwtToken parseToken(final String token) throws AuthenticationException;

    /**
     * @param req {@link HttpServletRequest}
     *
     * @return String
     */
    default String resolveToken(final HttpServletRequest req)
    {
        String bearerToken = req.getHeader("Authorization");

        if ((bearerToken != null) && bearerToken.startsWith("Bearer "))
        {
            return bearerToken.substring(7);
        }

        return null;
    }
}
