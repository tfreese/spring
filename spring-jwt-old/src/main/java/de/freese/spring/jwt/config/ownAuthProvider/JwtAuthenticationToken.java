// Created: 30.10.2018
package de.freese.spring.jwt.config.ownAuthProvider;

import java.io.Serial;
import java.util.Objects;

import org.springframework.security.authentication.AbstractAuthenticationToken;

/**
 * @author Thomas Freese
 */
class JwtAuthenticationToken extends AbstractAuthenticationToken {
    @Serial
    private static final long serialVersionUID = 3544121276547291346L;

    private final String token;

    JwtAuthenticationToken(final String token) {
        super(null);

        this.token = token;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }

        if (!super.equals(obj) || !(obj instanceof JwtAuthenticationToken other)) {
            return false;
        }

        return Objects.equals(this.token, other.token);
    }

    @Override
    public Object getCredentials() {
        return null;
    }

    @Override
    public Object getPrincipal() {
        return null;
    }

    public String getToken() {
        return this.token;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();

        result = (prime * result) + Objects.hash(this.token);

        return result;
    }
}
