// Created: 28.10.2018
package de.freese.spring.jwt.model;

import java.io.Serial;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.springframework.security.core.CredentialsContainer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * @author Thomas Freese
 */
public class MutableUser implements UserDetails, CredentialsContainer {
    @Serial
    private static final long serialVersionUID = -5966384384144097545L;

    private boolean accountNonExpired = true;
    private boolean accountNonLocked = true;
    private transient Set<? extends GrantedAuthority> authorities;
    private boolean credentialsNonExpired;
    private boolean enabled;
    private String password;
    private String username;

    public MutableUser() {
        super();
    }

    public MutableUser(final UserDetails userDetails) {
        super();

        this.accountNonExpired = userDetails.isAccountNonExpired();
        this.accountNonLocked = userDetails.isAccountNonLocked();
        this.authorities = new HashSet<>(userDetails.getAuthorities());
        this.credentialsNonExpired = userDetails.isCredentialsNonExpired();
        this.enabled = userDetails.isEnabled();
        this.password = userDetails.getPassword();
        this.username = userDetails.getUsername();
    }

    public MutableUser clearCredentials() {
        eraseCredentials();

        return this;
    }

    @Override
    public void eraseCredentials() {
        setPassword(null);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return this.authorities;
    }

    @Override
    public String getPassword() {
        return this.password;
    }

    @Override
    public String getUsername() {
        return this.username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return this.accountNonExpired;
    }

    @Override
    public boolean isAccountNonLocked() {
        return this.accountNonLocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return this.credentialsNonExpired;
    }

    @Override
    public boolean isEnabled() {
        return this.enabled;
    }

    public void setAccountNonExpired(final boolean accountNonExpired) {
        this.accountNonExpired = accountNonExpired;
    }

    public void setAccountNonLocked(final boolean accountNonLocked) {
        this.accountNonLocked = accountNonLocked;
    }

    public void setAuthorities(final Set<? extends GrantedAuthority> authorities) {
        this.authorities = authorities;
    }

    public void setCredentialsNonExpired(final boolean credentialsNonExpired) {
        this.credentialsNonExpired = credentialsNonExpired;
    }

    public void setPassword(final String password) {
        this.password = password;
    }

    public void setUsername(final String username) {
        this.username = username;
    }
}
