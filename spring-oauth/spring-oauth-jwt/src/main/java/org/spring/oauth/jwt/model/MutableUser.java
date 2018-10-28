/**
 * Created: 28.10.2018
 */

package org.spring.oauth.jwt.model;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * @author Thomas Freese
 */
public class MutableUser implements UserDetails
{
    /**
     *
     */
    private static final long serialVersionUID = -5966384384144097545L;

    /**
    *
    */
    private boolean accountNonExpired = true;

    /**
    *
    */
    private boolean accountNonLocked = true;

    /**
    *
    */
    private Set<? extends GrantedAuthority> authorities = null;

    /**
    *
    */
    private boolean credentialsNonExpired = true;

    /**
    *
    */
    private boolean enabled = false;

    /**
    *
    */
    private String password = null;

    /**
    *
    */
    private String username = null;

    /**
     * Erstellt ein neues {@link MutableUser} Object.
     */
    public MutableUser()
    {
        super();
    }

    /**
     * Erstellt ein neues {@link MutableUser} Object.
     *
     * @param userDetails {@link UserDetails}
     */
    public MutableUser(final UserDetails userDetails)
    {
        super();

        this.accountNonExpired = userDetails.isAccountNonExpired();
        this.accountNonLocked = userDetails.isAccountNonLocked();
        this.authorities = new HashSet<>(userDetails.getAuthorities());
        this.credentialsNonExpired = userDetails.isCredentialsNonExpired();
        this.enabled = userDetails.isEnabled();
        this.password = userDetails.getPassword();
        this.username = userDetails.getUsername();
    }

    /**
     * @return {@link MutableUser}
     */
    public MutableUser clearCredentials()
    {
        setPassword(null);

        return this;
    }

    /**
     * @see org.springframework.security.core.userdetails.UserDetails#getAuthorities()
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities()
    {
        return this.authorities;
    }

    /**
     * @see org.springframework.security.core.userdetails.UserDetails#getPassword()
     */
    @Override
    public String getPassword()
    {
        return this.password;
    }

    /**
     * @see org.springframework.security.core.userdetails.UserDetails#getUsername()
     */
    @Override
    public String getUsername()
    {
        return this.username;
    }

    /**
     * @see org.springframework.security.core.userdetails.UserDetails#isAccountNonExpired()
     */
    @Override
    public boolean isAccountNonExpired()
    {
        return this.accountNonExpired;
    }

    /**
     * @see org.springframework.security.core.userdetails.UserDetails#isAccountNonLocked()
     */
    @Override
    public boolean isAccountNonLocked()
    {
        return this.accountNonLocked;
    }

    /**
     * @see org.springframework.security.core.userdetails.UserDetails#isCredentialsNonExpired()
     */
    @Override
    public boolean isCredentialsNonExpired()
    {
        return this.credentialsNonExpired;
    }

    /**
     * @see org.springframework.security.core.userdetails.UserDetails#isEnabled()
     */
    @Override
    public boolean isEnabled()
    {
        return this.enabled;
    }

    /**
     * @param accountNonExpired boolean
     */
    public void setAccountNonExpired(final boolean accountNonExpired)
    {
        this.accountNonExpired = accountNonExpired;
    }

    /**
     * @param accountNonLocked boolean
     */
    public void setAccountNonLocked(final boolean accountNonLocked)
    {
        this.accountNonLocked = accountNonLocked;
    }

    /**
     * @param authorities {@link Set}<GrantedAuthority>
     */
    public void setAuthorities(final Set<? extends GrantedAuthority> authorities)
    {
        this.authorities = authorities;
    }

    /**
     * @param credentialsNonExpired boolean
     */
    public void setCredentialsNonExpired(final boolean credentialsNonExpired)
    {
        this.credentialsNonExpired = credentialsNonExpired;
    }

    /**
     * @param password String
     */
    public void setPassword(final String password)
    {
        this.password = password;
    }

    /**
     * @param username String
     */
    public void setUsername(final String username)
    {
        this.username = username;
    }
}
