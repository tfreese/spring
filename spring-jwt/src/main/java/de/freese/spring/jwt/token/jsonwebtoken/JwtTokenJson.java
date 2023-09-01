// Created: 22.12.2021
package de.freese.spring.jwt.token.jsonwebtoken;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwt;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import de.freese.spring.jwt.token.JwtToken;

/**
 * @author Thomas Freese
 */
class JwtTokenJson implements JwtToken {
    private final Jwt<?, Jws<Claims>> jwt;

    JwtTokenJson(final Jwt<?, Jws<Claims>> jwt) {
        super();

        this.jwt = Objects.requireNonNull(jwt, "jwt required");
    }

    @Override
    public Date getExpirationDate() {
        return getClaimsValue(Claims::getExpiration);
    }

    @Override
    public String getPassword() {
        return getClaimsValue(claims -> (String) claims.get("password"));
    }

    @Override
    public Set<GrantedAuthority> getRoles() {
        String rolesString = getClaimsValue(claims -> (String) claims.get("roles"));

        if ((rolesString == null) || rolesString.isBlank()) {
            return Collections.emptySet();
        }

        String[] rolesArray = rolesString.split(",");

        return Arrays.stream(rolesArray).map(SimpleGrantedAuthority::new).collect(Collectors.toSet());
    }

    @Override
    public String getUsername() {
        return getClaimsValue(Claims::getSubject);
    }

    private <T> T getClaimsValue(final Function<Claims, T> function) {
        return function.apply(this.jwt.getBody().getBody());
    }
}
