// Created: 22.12.2021
package de.freese.spring.jwt.token;

import java.util.Date;
import java.util.Set;

import org.springframework.security.core.GrantedAuthority;

/**
 * @author Thomas Freese
 */
public interface JwtToken {
    Date getExpirationDate();

    String getPassword();

    Set<GrantedAuthority> getRoles();

    String getUsername();

    default boolean isExpired() {
        final Date expiration = getExpirationDate();

        return expiration.before(new Date());
    }
}
