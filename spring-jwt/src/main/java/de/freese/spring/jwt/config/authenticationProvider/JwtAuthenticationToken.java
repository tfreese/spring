// Created: 30.10.2018
package de.freese.spring.jwt.config.authenticationProvider;

import java.util.Collection;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

/**
 * @author Thomas Freese
 */
class JwtAuthenticationToken extends AbstractAuthenticationToken
{
    /**
     *
     */
    private static final long serialVersionUID = 3544121276547291346L;
    /**
     *
     */
    private Object credentials;
    /**
     *
     */
    private Object principal;
    /**
     *
     */
    private final String token;

    /**
     * Erstellt ein neues {@link JwtAuthenticationToken} Object.
     *
     * @param principal Object
     * @param credentials Object
     * @param authorities {@link Collection}
     */
    public JwtAuthenticationToken(final Object principal, final Object credentials, final Collection<? extends GrantedAuthority> authorities)
    {
        super(authorities);

        this.token = null;
        this.principal = principal;
        this.credentials = credentials;
    }

    /**
     * Erstellt ein neues {@link JwtAuthenticationToken} Object.
     *
     * @param token String
     */
    public JwtAuthenticationToken(final String token)
    {
        super(null);

        this.token = token;
        this.principal = null;
        this.credentials = null;
    }

    /**
     * @see org.springframework.security.authentication.AbstractAuthenticationToken#eraseCredentials()
     */
    @Override
    public void eraseCredentials()
    {
        super.eraseCredentials();

        this.credentials = null;
    }

    /**
     * @see org.springframework.security.core.Authentication#getCredentials()
     */
    @Override
    public Object getCredentials()
    {
        return this.credentials;
    }

    /**
     * @see org.springframework.security.core.Authentication#getPrincipal()
     */
    @Override
    public Object getPrincipal()
    {
        return this.principal;
    }

    /**
     * @return String
     */
    public String getToken()
    {
        return this.token;
    }
}
