/**
 * Created: 28.10.2018
 */

package org.spring.oauth.jwt.service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import org.spring.oauth.jwt.config.JwtTokenProvider;
import org.spring.oauth.jwt.exception.MyJwtException;
import org.spring.oauth.jwt.model.MutableUser;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.stereotype.Service;

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
     * @param userName String
     */
    public void delete(final String userName)
    {
        this.userDetailsManager.deleteUser(userName);
    }

    /**
     * @param userName String
     * @return {@link UserDetails}
     */
    public UserDetails search(final String userName)
    {
        UserDetails userDetails = this.userDetailsManager.loadUserByUsername(userName);

        if (userDetails == null)
        {
            throw new MyJwtException("The user doesn't exist", HttpStatus.NOT_FOUND);
        }

        return userDetails;
    }

    /**
     * Login
     *
     * @param userName String
     * @param password String
     * @return String
     */
    public String signin(final String userName, final String password)
    {
        try
        {
            this.authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(userName, password));

            UserDetails userDetails = this.userDetailsManager.loadUserByUsername(userName);

            return this.jwtTokenProvider.createToken(userName, userDetails.getAuthorities());
        }
        catch (AuthenticationException ex)
        {
            throw new MyJwtException("Invalid username/password supplied", HttpStatus.UNPROCESSABLE_ENTITY);
        }
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

            return this.jwtTokenProvider.createToken(mutableUser.getUsername(), mutableUser.getAuthorities());
        }

        throw new MyJwtException("Username is already in use", HttpStatus.UNPROCESSABLE_ENTITY);
    }

    /**
     * @param req {@link HttpServletRequest}
     * @return {@link UserDetails}
     */
    public UserDetails whoami(final HttpServletRequest req)
    {
        String jwtToken = this.jwtTokenProvider.resolveToken(req);
        String userName = this.jwtTokenProvider.getUsername(jwtToken);

        return this.userDetailsManager.loadUserByUsername(userName);
    }
}
