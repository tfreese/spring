// Created: 30.10.2018
package de.freese.spring.jwt.config.simple;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.web.filter.OncePerRequestFilter;

import de.freese.spring.jwt.token.JwtTokenUtils;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.SignatureException;

/**
 * @see BasicAuthenticationFilter
 *
 * @author Thomas Freese
 */
public class JwtRequestFilterSimple extends OncePerRequestFilter
{
    /**
    *
    */
    private static final Logger LOGGER = LoggerFactory.getLogger(JwtRequestFilterSimple.class);

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
        // JWT Token is in the form "Bearer token".
        // Remove Bearer word and get only the Token.
        String bearerToken = request.getHeader("Authorization");

        String jwtToken = null;
        Jws<Claims> claims = null;
        String username = null;
        String password = null;
        // Set<GrantedAuthority> authorities = null;

        if ((bearerToken != null) && bearerToken.startsWith("Bearer "))
        {
            jwtToken = bearerToken.substring(7);

            try
            {
                claims = this.jwtTokenUtils.parseToken(jwtToken);

                username = this.jwtTokenUtils.getUsername(claims);
                password = this.jwtTokenUtils.getPassword(claims);
                // authorities = this.jwtTokenUtils.getRoles(claims);
            }
            catch (IllegalArgumentException ex)
            {
                getLogger().error("Unable to get JWT Token", ex);
            }
            catch (ExpiredJwtException ex)
            {
                getLogger().error("JWT Token has expired", ex);
            }
            catch (SignatureException ex)
            {
                getLogger().error("Authentication Failed. Username or Password not valid", ex);
            }
        }

        // Once we get the token validate it.
        if ((username != null) && isAuthenticationIsRequired(username))
        {
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);

            // Hier müssten noch die ganzen Prüfungen auf Expire, Active und Password stattfinden wie im DaoAuthenticationProvider.
            if (this.jwtTokenUtils.validateToken(claims, userDetails) && this.passwordEncoder.matches(password, userDetails.getPassword()))
            {
                UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                usernamePasswordAuthenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // After setting the Authentication in the context, we specify that the current user is authenticated.
                // So it passes the Spring Security Configurations successfully.
                SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
            }
            else
            {
                SecurityContextHolder.clearContext();
            }
        }

        filterChain.doFilter(request, response);
    }

    /**
     * @return {@link Logger}
     */
    protected Logger getLogger()
    {
        return LOGGER;
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
