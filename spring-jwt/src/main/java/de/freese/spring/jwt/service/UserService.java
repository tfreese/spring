// Created: 28.10.2018
package de.freese.spring.jwt.service;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import de.freese.spring.jwt.model.MutableUser;
import de.freese.spring.jwt.token.JwtToken;
import de.freese.spring.jwt.token.JwtTokenProvider;

/**
 * @author Thomas Freese
 */
@Service
public class UserService {
    @Resource
    private AuthenticationManager authenticationManager;

    @Resource
    private JwtTokenProvider jwtTokenProvider;

    @Resource
    private PasswordEncoder passwordEncoder;

    @Resource
    private UserDetailsService userDetailsService;

    public void delete(final String username) {
        throw new AuthenticationServiceException("Need a UserDetailsManager");
        //this.userDetailsManager.deleteUser(username);
    }

    public String login(final String username, final String password) {
        this.authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));

        // final UserDetails userDetails = this.userDetailsManager.loadUserByUsername(username);

        return this.jwtTokenProvider.createToken(username, password);
    }

    public String register(final UserDetails userDetails) {
        throw new AuthenticationServiceException("Need a UserDetailsManager");

        //        final boolean exist = this.userDetailsManager.userExists(userDetails.getUsername());
        //
        //        if (!exist)
        //        {
        //            final MutableUser mutableUser = new MutableUser(userDetails);
        //            mutableUser.setPassword(this.passwordEncoder.encode(userDetails.getPassword()));
        //
        //            this.userDetailsManager.createUser(mutableUser);
        //
        //            return this.jwtTokenProvider.createToken(mutableUser.getUsername(), userDetails.getPassword(), mutableUser.getAuthorities());
        //        }
        //
        //        throw new AuthenticationServiceException("Username is already in use");
    }

    public UserDetails search(final String username) {
        final UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);

        if (userDetails == null) {
            throw new UsernameNotFoundException("The user doesn't exist");
        }

        return new MutableUser(userDetails).clearCredentials();
    }

    public UserDetails whoami(final HttpServletRequest req) {
        final String token = this.jwtTokenProvider.resolveToken(req);
        final JwtToken jwtToken = this.jwtTokenProvider.parseToken(token);
        final String username = jwtToken.getUsername();

        final UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);

        return new MutableUser(userDetails).clearCredentials();
    }
}
