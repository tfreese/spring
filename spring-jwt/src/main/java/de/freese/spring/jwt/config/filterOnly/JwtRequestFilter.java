// Created: 30.10.2018
package de.freese.spring.jwt.config.filterOnly;

import java.io.IOException;
import java.util.Objects;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AccountExpiredException;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.web.filter.OncePerRequestFilter;

import de.freese.spring.jwt.token.JwtTokenUtils;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;

/**
 * Der {@link JwtRequestFilter} verwendet keinen {@link AuthenticationProvider},<br>
 * sondern validiert das Token mit Passwort-Vergleich, Gültigkeit etc. selber und setzt es in den {@link SecurityContext}..
 *
 * @see BasicAuthenticationFilter
 *
 * @author Thomas Freese
 */
class JwtRequestFilter extends OncePerRequestFilter
{
    /**
    *
    */
    private static final Logger LOGGER = LoggerFactory.getLogger(JwtRequestFilter.class);

    /**
    *
    */
    private AuthenticationEntryPoint authenticationEntryPoint;
    /**
     *
     */
    private JwtTokenUtils jwtTokenUtils;
    /**
    *
    */
    private PasswordEncoder passwordEncoder;
    /**
     *
     */
    private UserDetailsService userDetailsService;

    /**
     * @see org.springframework.web.filter.OncePerRequestFilter#doFilterInternal(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse,
     *      javax.servlet.FilterChain)
     */
    @Override
    protected void doFilterInternal(final HttpServletRequest request, final HttpServletResponse response, final FilterChain filterChain)
        throws ServletException, IOException
    {
        String token = this.jwtTokenUtils.resolveToken(request);

        try
        {
            String username = null;
            String password = null;

            if (token != null)
            {
                Jws<Claims> claims = this.jwtTokenUtils.parseToken(token);

                username = this.jwtTokenUtils.getUsername(claims);
                password = this.jwtTokenUtils.getPassword(claims);
            }

            if ((username != null) && isAuthenticationIsRequired(username))
            {
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);

                if (isValid(userDetails, password))
                {
                    UsernamePasswordAuthenticationToken authResult = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    authResult.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    SecurityContextHolder.getContext().setAuthentication(authResult);
                    // SecurityContext context = SecurityContextHolder.createEmptyContext();
                    // context.setAuthentication(authResult);
                    // SecurityContextHolder.setContext(context);
                }
                else
                {
                    SecurityContextHolder.clearContext();
                }
            }
        }
        catch (AuthenticationException ex)
        {
            SecurityContextHolder.clearContext();

            getLogger().error("Authentication request failed: {}", ex.getMessage());

            // Deswegen würden Tests der Logins über den RestController nicht mehr funktionieren !
            this.authenticationEntryPoint.commence(request, response, ex);

            return;
        }

        filterChain.doFilter(request, response);
    }

    /**
     * @return {@link Logger}
     */
    private Logger getLogger()
    {
        return LOGGER;
    }

    /**
     * @see org.springframework.web.filter.GenericFilterBean#initFilterBean()
     */
    @Override
    protected void initFilterBean() throws ServletException
    {
        super.initFilterBean();

        Objects.requireNonNull(this.authenticationEntryPoint, "authenticationEntryPoint requried");
        Objects.requireNonNull(this.userDetailsService, "userDetailsService requried");
        Objects.requireNonNull(this.passwordEncoder, "passwordEncoder requried");
        Objects.requireNonNull(this.jwtTokenUtils, "jwtTokenUtils requried");
    }

    /**
     * @param username String
     *
     * @return boolean
     */
    private boolean isAuthenticationIsRequired(final String username)
    {
        Authentication existingAuth = SecurityContextHolder.getContext().getAuthentication();

        if ((existingAuth == null) || !existingAuth.isAuthenticated()
                || ((existingAuth instanceof UsernamePasswordAuthenticationToken) && !existingAuth.getName().equals(username)))
        {
            return true;
        }

        return (existingAuth instanceof AnonymousAuthenticationToken);
    }

    /**
     * @param userDetails {@link UserDetails}
     * @param password String
     *
     * @return boolean
     *
     * @throws AuthenticationException Falls was schief geht.
     *
     * @see DaoAuthenticationProvider
     */
    private boolean isValid(final UserDetails userDetails, final String password) throws AuthenticationException
    {
        if (userDetails == null)
        {
            return false;
        }

        if (!userDetails.isAccountNonLocked())
        {
            getLogger().error("Failed to authenticate since user account is locked");
            throw new LockedException("User account is locked");
        }

        if (!userDetails.isEnabled())
        {
            getLogger().error("Failed to authenticate since user account is disabled");
            throw new DisabledException("User is disabled");
        }

        if (!userDetails.isAccountNonExpired())
        {
            getLogger().error("Failed to authenticate since user account has expired");
            throw new AccountExpiredException("User account has expired");
        }

        if (!userDetails.isCredentialsNonExpired())
        {
            getLogger().error("Failed to authenticate since user account credentials have expired");
            throw new CredentialsExpiredException("User credentials have expired");
        }

        if (userDetails.getPassword() == null)
        {
            getLogger().error("Failed to authenticate since no credentials provided");
            throw new BadCredentialsException("Bad credentials");
        }

        if (!this.passwordEncoder.matches(password, userDetails.getPassword()))
        {
            getLogger().error("Failed to authenticate since password does not match stored value");
            throw new BadCredentialsException("Bad credentials");
        }

        return true;
    }

    /**
     * @param authenticationEntryPoint {@link AuthenticationEntryPoint}
     */
    public void setAuthenticationEntryPoint(final AuthenticationEntryPoint authenticationEntryPoint)
    {
        this.authenticationEntryPoint = authenticationEntryPoint;
    }

    /**
     * @param jwtTokenUtils {@link JwtTokenUtils}
     */
    public void setJwtTokenUtils(final JwtTokenUtils jwtTokenUtils)
    {
        this.jwtTokenUtils = jwtTokenUtils;
    }

    /**
     * @param passwordEncoder {@link PasswordEncoder}
     */
    public void setPasswordEncoder(final PasswordEncoder passwordEncoder)
    {
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * @param userDetailsService {@link UserDetailsService}
     */
    public void setUserDetailsService(final UserDetailsService userDetailsService)
    {
        this.userDetailsService = userDetailsService;
    }
}
