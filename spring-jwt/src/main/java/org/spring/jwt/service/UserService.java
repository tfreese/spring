/**
 * Created: 28.10.2018
 */

package org.spring.jwt.service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import org.spring.jwt.model.MutableUser;
import org.spring.jwt.token.JwtTokenProvider;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.stereotype.Service;
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
    private AuthenticationManager authenticationManager = null;

    /**
     *
     */
    @Resource
    private JwtTokenProvider jwtTokenProvider = null;

    /**
     *
     */
    @Resource
    private PasswordEncoder passwordEncoder = null;

    /**
    *
    */
    @Resource
    private UserDetailsManager userDetailsManager = null;

    /**
     * Erstellt ein neues {@link UserService} Object.
     */
    public UserService()
    {
        super();
    }

    /**
     * @param username String
     */
    public void delete(final String username)
    {
        this.userDetailsManager.deleteUser(username);
    }

    /**
     * @param username String
     * @return {@link UserDetails}
     */
    public UserDetails search(final String username)
    {
        UserDetails userDetails = this.userDetailsManager.loadUserByUsername(username);

        if (userDetails == null)
        {
            throw new UsernameNotFoundException("The user doesn't exist");
        }

        MutableUser mutableUser = new MutableUser(userDetails).clearCredentials();

        return mutableUser;
    }

    /**
     * Login
     *
     * @param username String
     * @param password String
     * @return String
     * @throws AuthenticationException Falls was schief geht.
     */
    public String signin(final String username, final String password) throws AuthenticationException
    {
        this.authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));

        // UserDetails userDetails = this.userDetailsManager.loadUserByUsername(username);

        return this.jwtTokenProvider.createToken(username, password);
    }

    /**
     * Registrierung
     *
     * @param userDetails {@link UserDetails}
     * @return String
     */
    public String signup(final UserDetails userDetails)
    {
        boolean exist = this.userDetailsManager.userExists(userDetails.getUsername());

        if (!exist)
        {
            MutableUser mutableUser = new MutableUser(userDetails);
            mutableUser.setPassword(this.passwordEncoder.encode(userDetails.getPassword()));

            this.userDetailsManager.createUser(mutableUser);

            return this.jwtTokenProvider.createToken(mutableUser.getUsername(), userDetails.getPassword(), mutableUser.getAuthorities());
        }

        throw new AuthenticationServiceException("Username is already in use");
    }

    /**
     * @param req {@link HttpServletRequest}
     * @return {@link UserDetails}
     */
    public UserDetails whoami(final HttpServletRequest req)
    {
        String jwtToken = this.jwtTokenProvider.resolveToken(req);
        Jws<Claims> claims = this.jwtTokenProvider.parseToken(jwtToken);
        String username = this.jwtTokenProvider.getUsername(claims);

        UserDetails userDetails = this.userDetailsManager.loadUserByUsername(username);

        MutableUser mutableUser = new MutableUser(userDetails).clearCredentials();

        return mutableUser;
    }
}
