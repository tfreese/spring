// Created: 28.10.2018
package de.freese.spring.jwt.service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.stereotype.Service;

import de.freese.spring.jwt.model.MutableUser;
import de.freese.spring.jwt.token.JwtTokenUtils;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;

/**
 * @author Thomas Freese
 */
@Service
public class UserService
{
    /**
     *
     */
    @Resource
    private AuthenticationManager authenticationManager;
    /**
     *
     */
    @Resource
    private JwtTokenUtils jwtTokenUtils;
    /**
     *
     */
    @Resource
    private PasswordEncoder passwordEncoder;
    /**
     *
     */
    @Resource
    private UserDetailsManager userDetailsManager;

    /**
     * @param username String
     */
    public void delete(final String username)
    {
        this.userDetailsManager.deleteUser(username);
    }

    /**
     * Login
     *
     * @param username String
     * @param password String
     *
     * @return String
     */
    public String login(final String username, final String password)
    {
        this.authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));

        // UserDetails userDetails = this.userDetailsManager.loadUserByUsername(username);

        return this.jwtTokenUtils.createToken(username, password);
    }

    /**
     * Registrierung
     *
     * @param userDetails {@link UserDetails}
     *
     * @return String
     */
    public String register(final UserDetails userDetails)
    {
        boolean exist = this.userDetailsManager.userExists(userDetails.getUsername());

        if (!exist)
        {
            MutableUser mutableUser = new MutableUser(userDetails);
            mutableUser.setPassword(this.passwordEncoder.encode(userDetails.getPassword()));

            this.userDetailsManager.createUser(mutableUser);

            return this.jwtTokenUtils.createToken(mutableUser.getUsername(), userDetails.getPassword(), mutableUser.getAuthorities());
        }

        throw new AuthenticationServiceException("Username is already in use");
    }

    /**
     * @param username String
     *
     * @return {@link UserDetails}
     */
    public UserDetails search(final String username)
    {
        UserDetails userDetails = this.userDetailsManager.loadUserByUsername(username);

        if (userDetails == null)
        {
            throw new UsernameNotFoundException("The user doesn't exist");
        }

        return new MutableUser(userDetails).clearCredentials();
    }

    /**
     * @param req {@link HttpServletRequest}
     *
     * @return {@link UserDetails}
     */
    public UserDetails whoami(final HttpServletRequest req)
    {
        String jwtToken = this.jwtTokenUtils.resolveToken(req);
        Jws<Claims> claims = this.jwtTokenUtils.parseToken(jwtToken);
        String username = this.jwtTokenUtils.getUsername(claims);

        UserDetails userDetails = this.userDetailsManager.loadUserByUsername(username);

        return new MutableUser(userDetails).clearCredentials();
    }
}
