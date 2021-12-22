// Created: 22.12.2021
package de.freese.spring.jwt.token;

import java.util.Date;
import java.util.Set;

import org.springframework.security.core.GrantedAuthority;

/**
 * @author Thomas Freese
 */
public interface JwtToken
{
    /**
     * @return {@link Date}
     */
    Date getExpirationDate();

    /**
     * @return String
     */
    String getPassword();

    /**
     * @return {@link Set}
     */
    Set<GrantedAuthority> getRoles();

    /**
     * @return String
     */
    String getUsername();

    /**
     * @return boolean
     */
    default boolean isExpired()
    {
        final Date expiration = getExpirationDate();

        return expiration.before(new Date());
    }
}
