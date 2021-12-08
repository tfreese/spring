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
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.web.filter.OncePerRequestFilter;

import de.freese.spring.jwt.token.JwtTokenUtils;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.SignatureException;

/**
 * Diese Variante benutzt den {@link DaoAuthenticationProvider}.
 *
 * @see BasicAuthenticationFilter
 *
 * @author Thomas Freese
 */
public class JwtRequestFilterSimple2 extends OncePerRequestFilter
{
    /**
    *
    */
    private static final Logger LOGGER = LoggerFactory.getLogger(JwtRequestFilterSimple2.class);

    /**
    *
    */
    private AuthenticationEntryPoint authenticationEntryPoint;
    /**
    *
    */
    private AuthenticationManager authenticationManager;
    /**
     *
     */
    private JwtTokenUtils jwtTokenUtils;

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

        try
        {

            String jwtToken = null;
            Jws<Claims> claims = null;
            String username = null;
            String password = null;

            if ((bearerToken != null) && bearerToken.startsWith("Bearer "))
            {
                jwtToken = bearerToken.substring(7);

                try
                {
                    claims = this.jwtTokenUtils.parseToken(jwtToken);

                    username = this.jwtTokenUtils.getUsername(claims);
                    password = this.jwtTokenUtils.getPassword(claims);
                }
                catch (IllegalArgumentException ex)
                {
                    getLogger().error("Unable to get JWT Token", ex);

                    throw new AuthenticationServiceException("Unable to get JWT Token");
                }
                catch (ExpiredJwtException ex)
                {
                    getLogger().error("JWT Token has expired", ex);

                    throw new AuthenticationServiceException("JWT Token has expired");
                }
                catch (SignatureException ex)
                {
                    getLogger().error("Authentication Failed. Username or Password not valid", ex);

                    throw new BadCredentialsException("JWT Token has expired");
                }
            }

            // Once we get the token validate it.
            if ((username != null) && isAuthenticationIsRequired(username))
            {
                UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(username, password);
                usernamePasswordAuthenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                Authentication authResult = this.authenticationManager.authenticate(usernamePasswordAuthenticationToken);

                SecurityContext context = SecurityContextHolder.createEmptyContext();
                context.setAuthentication(authResult);
                SecurityContextHolder.setContext(context);

                // UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);
                //
                // // If token is valid configure Spring Security to manually set authentication.
                // if (this.jwtTokenUtils.validateToken(claims, userDetails))
                // {
                // UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken =
                // new UsernamePasswordAuthenticationToken(userDetails, password, userDetails.getAuthorities());
                // usernamePasswordAuthenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                //
                // Authentication authResult = this.authenticationManager.authenticate(usernamePasswordAuthenticationToken);
                //
                // SecurityContext context = SecurityContextHolder.createEmptyContext();
                // context.setAuthentication(authResult);
                // SecurityContextHolder.setContext(context);
                // }
                // else
                // {
                // throw new AuthenticationServiceException("Invalid JWT Token");
                // }
            }
        }
        catch (AuthenticationException ex)
        {
            SecurityContextHolder.clearContext();

            getLogger().debug("Authentication request failed: ", ex);

            // Deswegen würden Test die Logins über den RestController nicht mehr funktionieren !
            this.authenticationEntryPoint.commence(request, response, ex);

            return;
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
     * @param authenticationEntryPoint {@link AuthenticationEntryPoint}
     */
    public void setAuthenticationEntryPoint(final AuthenticationEntryPoint authenticationEntryPoint)
    {
        this.authenticationEntryPoint = authenticationEntryPoint;
    }

    /**
     * @param authenticationManager {@link AuthenticationManager}
     */
    public void setAuthenticationManager(final AuthenticationManager authenticationManager)
    {
        this.authenticationManager = authenticationManager;
    }

    /**
     * @param jwtTokenUtils {@link JwtTokenUtils}
     */
    public void setJwtTokenUtils(final JwtTokenUtils jwtTokenUtils)
    {
        this.jwtTokenUtils = jwtTokenUtils;
    }
}
