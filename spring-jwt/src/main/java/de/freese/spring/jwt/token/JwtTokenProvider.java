// Created: 22.12.2021
package de.freese.spring.jwt.token;

import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;

/**
 * @author Thomas Freese
 */
public interface JwtTokenProvider {
    default String createToken(final String username, final String password) {
        final Set<String> roles = null;

        return createToken(username, password, roles);
    }

    default String createToken(final String username, final String password, final Collection<? extends GrantedAuthority> roles) {
        Set<String> rolesSet = null;

        if ((roles != null) && !roles.isEmpty()) {
            // @formatter:off
            rolesSet = roles.stream()
                    .filter(Objects::nonNull)
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toSet())
                    ;
            // @formatter:on
        }

        return createToken(username, password, rolesSet);
    }

    String createToken(String username, String password, Set<String> roles);

    JwtToken parseToken(String token) throws AuthenticationException;

    default String resolveToken(final HttpServletRequest req) {
        final String bearerToken = req.getHeader("Authorization");

        if ((bearerToken != null) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }

        return null;
    }
}
