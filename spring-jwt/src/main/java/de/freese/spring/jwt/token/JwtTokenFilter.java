/**
 * Created: 30.10.2018
 */

package de.freese.spring.jwt.token;

import java.io.IOException;
import java.util.Objects;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationDetailsSource;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * @author Thomas Freese
 */
public class JwtTokenFilter extends OncePerRequestFilter
{
    /**
    *
    */
    private static final Logger LOGGER = LoggerFactory.getLogger(JwtTokenFilter.class);

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
     * Erstellt ein neues {@link JwtTokenFilter} Object.
     */
    public JwtTokenFilter()
    {
        super();
    }

    /**
     * @see org.springframework.web.filter.OncePerRequestFilter#doFilterInternal(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse,
     *      javax.servlet.FilterChain)
     */
    @Override
    protected void doFilterInternal(final HttpServletRequest request, final HttpServletResponse response, final FilterChain filterChain)
        throws ServletException, IOException
    {
        String bearerToken = request.getHeader("Authorization");

        String jwtToken = null;

        if ((bearerToken != null) && bearerToken.startsWith("Bearer "))
        {
            jwtToken = bearerToken.substring(7, bearerToken.length());
        }

        try
        {
            if ((jwtToken != null) && !jwtToken.isBlank())
            {
                getLogger().debug("JwtToken Pre-Authentication Authorization header found");

                JwtAuthenticationToken authRequest = new JwtAuthenticationToken(jwtToken);
                authRequest.setDetails(this.authenticationDetailsSource.buildDetails(request));

                Authentication authResult = this.authenticationManager.authenticate(authRequest);

                getLogger().debug("Authentication success: {}", authResult);

                SecurityContextHolder.getContext().setAuthentication(authResult);
            }
        }
        catch (AuthenticationException ex)
        {
            SecurityContextHolder.clearContext();

            getLogger().debug("Authentication request failed: ", ex);

            this.authenticationEntryPoint.commence(request, response, ex);

            return;
        }

        filterChain.doFilter(request, response);
    }

    /**
     * @return {@link Logger}
     */
    public Logger getLogger()
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

        Objects.requireNonNull(this.authenticationManager, "authenticationManager requried");
        Objects.requireNonNull(this.authenticationEntryPoint, "authenticationEntryPoint requried");
        Objects.requireNonNull(this.authenticationDetailsSource, "authenticationDetailsSource requried");
    }

    /**
     * @param authenticationDetailsSource {@link AuthenticationDetailsSource}
     */
    public void setAuthenticationDetailsSource(final AuthenticationDetailsSource<HttpServletRequest, ?> authenticationDetailsSource)
    {
        this.authenticationDetailsSource = authenticationDetailsSource;
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
}
