/**
 * Created: 28.10.2018
 */

package org.spring.oauth.jwt.config;

import java.io.IOException;
import java.util.Objects;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationDetailsSource;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * @author Thomas Freese
 */
public class JwtTokenFilter2 extends OncePerRequestFilter
{
    /**
    *
    */
    private static final Logger LOGGER = LoggerFactory.getLogger(JwtTokenFilter2.class);

    /**
    *
    */
    private AuthenticationDetailsSource<HttpServletRequest, ?> authenticationDetailsSource = new WebAuthenticationDetailsSource();

    /**
        *
        */
    private AuthenticationEntryPoint authenticationEntryPoint = null;

    /**
    *
    */
    private AuthenticationManager authenticationManager = null;

    /**
    *
    */
    private boolean ignoreFailure = false;

    /**
    *
    */
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * Erstellt ein neues {@link JwtTokenFilter2} Object.
     *
     * @param jwtTokenProvider {@link JwtTokenProvider}
     * @param authenticationManager {@link AuthenticationManager}
     * @param authenticationEntryPoint {@link AuthenticationEntryPoint}
     */
    public JwtTokenFilter2(final JwtTokenProvider jwtTokenProvider, final AuthenticationManager authenticationManager,
            final AuthenticationEntryPoint authenticationEntryPoint)
    {
        super();

        this.jwtTokenProvider = Objects.requireNonNull(jwtTokenProvider, "jwtTokenProvider required");
        this.authenticationManager = Objects.requireNonNull(authenticationManager, "authenticationManager required");
        this.authenticationEntryPoint = Objects.requireNonNull(authenticationEntryPoint, "authenticationEntryPoint required");
    }

    /**
     * @param userName String
     * @return boolean
     */
    protected boolean authenticationIsRequired(final String userName)
    {
        Authentication existingAuth = SecurityContextHolder.getContext().getAuthentication();

        if ((existingAuth == null) || !existingAuth.isAuthenticated())
        {
            return true;
        }

        if ((existingAuth instanceof UsernamePasswordAuthenticationToken) && !existingAuth.getName().equals(userName))
        {
            return true;
        }

        if ((existingAuth instanceof PreAuthenticatedAuthenticationToken) && !existingAuth.getName().equals(userName))
        {
            return true;
        }

        if (existingAuth instanceof AnonymousAuthenticationToken)
        {
            return true;
        }

        return false;
    }

    /**
     * @see org.springframework.web.filter.OncePerRequestFilter#doFilterInternal(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse,
     *      javax.servlet.FilterChain)
     */
    @Override
    protected void doFilterInternal(final HttpServletRequest request, final HttpServletResponse response, final FilterChain filterChain)
        throws ServletException, IOException
    {
        String token = this.jwtTokenProvider.resolveToken(request);

        try
        {
            if (token != null)
            {
                LOGGER.debug("JwtToken Pre-Authentication Authorization header found");

                if (this.jwtTokenProvider.validateToken(token))
                {

                    Authentication authRequest = this.jwtTokenProvider.getAuthentication(token);

                    if (authRequest instanceof AbstractAuthenticationToken)
                    {
                        ((AbstractAuthenticationToken) authRequest).setDetails(this.authenticationDetailsSource.buildDetails(request));
                    }

                    Authentication authResult = this.authenticationManager.authenticate(authRequest);

                    LOGGER.debug("Authentication success: " + authResult);

                    SecurityContextHolder.getContext().setAuthentication(authResult);

                    // this.rememberMeServices.loginSuccess(request, response, authResult);
                }
            }
        }
        catch (AuthenticationException ex)
        {
            SecurityContextHolder.clearContext();

            LOGGER.debug("Authentication request for failed: ", ex);

            // this.rememberMeServices.loginFail(request, response);

            // response.sendError(ex.getHttpStatus().value(), ex.getMessage());

            if (isIgnoreFailure())
            {
                filterChain.doFilter(request, response);
            }
            else
            {
                this.authenticationEntryPoint.commence(request, response, ex);
            }

            return;
        }

        filterChain.doFilter(request, response);
    }

    /**
     * @return boolean
     */
    protected boolean isIgnoreFailure()
    {
        return this.ignoreFailure;
    }

    /**
     * @param authenticationDetailsSource {@link AuthenticationDetailsSource}
     */
    public void setAuthenticationDetailsSource(final AuthenticationDetailsSource<HttpServletRequest, ?> authenticationDetailsSource)
    {
        this.authenticationDetailsSource = Objects.requireNonNull(authenticationDetailsSource, "authenticationDetailsSource required");
    }

    /**
     * @param ignoreFailure boolean
     */
    public void setIgnoreFailure(final boolean ignoreFailure)
    {
        this.ignoreFailure = ignoreFailure;
    }
}