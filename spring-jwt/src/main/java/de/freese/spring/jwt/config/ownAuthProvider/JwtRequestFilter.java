// Created: 30.10.2018
package de.freese.spring.jwt.config.ownAuthProvider;

import java.io.IOException;
import java.util.Objects;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationDetailsSource;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Der {@link JwtRequestFilter} verwendet den {@link JwtTokenAuthenticationProvider}.<br>
 *
 * @author Thomas Freese
 * @see BasicAuthenticationFilter
 */
class JwtRequestFilter extends OncePerRequestFilter {
    private static final Logger LOGGER = LoggerFactory.getLogger(JwtRequestFilter.class);

    private AuthenticationDetailsSource<HttpServletRequest, ?> authenticationDetailsSource = new WebAuthenticationDetailsSource();

    private AuthenticationEntryPoint authenticationEntryPoint;

    private AuthenticationManager authenticationManager;

    public void setAuthenticationDetailsSource(final AuthenticationDetailsSource<HttpServletRequest, ?> authenticationDetailsSource) {
        this.authenticationDetailsSource = authenticationDetailsSource;
    }

    public void setAuthenticationEntryPoint(final AuthenticationEntryPoint authenticationEntryPoint) {
        this.authenticationEntryPoint = authenticationEntryPoint;
    }

    public void setAuthenticationManager(final AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

    /**
     * @see org.springframework.web.filter.OncePerRequestFilter#doFilterInternal(jakarta.servlet.http.HttpServletRequest, jakarta.servlet.http.HttpServletResponse,
     * jakarta.servlet.FilterChain)
     */
    @Override
    protected void doFilterInternal(final HttpServletRequest request, final HttpServletResponse response, final FilterChain filterChain) throws ServletException, IOException {
        String bearerToken = request.getHeader("Authorization");

        String token = null;

        if ((bearerToken != null) && bearerToken.startsWith("Bearer ")) {
            token = bearerToken.substring(7);
        }

        try {
            if ((token != null) && !token.isBlank()) {
                getLogger().debug("JwtToken Pre-Authentication Authorization header found");

                JwtAuthenticationToken authRequest = new JwtAuthenticationToken(token);
                authRequest.setDetails(this.authenticationDetailsSource.buildDetails(request));

                Authentication authResult = this.authenticationManager.authenticate(authRequest);

                getLogger().debug("Authentication success: {}", authResult);

                SecurityContextHolder.getContext().setAuthentication(authResult);
                // SecurityContext context = SecurityContextHolder.createEmptyContext();
                // context.setAuthentication(authResult);
                // SecurityContextHolder.setContext(context);
            }
        }
        catch (AuthenticationException ex) {
            SecurityContextHolder.clearContext();

            getLogger().debug("Authentication request failed: {}", ex.getMessage());

            // Deswegen würden Tests der Logins über den RestController nicht mehr funktionieren !
            this.authenticationEntryPoint.commence(request, response, ex);

            return;
        }

        filterChain.doFilter(request, response);
    }

    /**
     * @see org.springframework.web.filter.GenericFilterBean#initFilterBean()
     */
    @Override
    protected void initFilterBean() throws ServletException {
        super.initFilterBean();

        Objects.requireNonNull(this.authenticationManager, "authenticationManager required");
        Objects.requireNonNull(this.authenticationEntryPoint, "authenticationEntryPoint required");
        Objects.requireNonNull(this.authenticationDetailsSource, "authenticationDetailsSource required");
    }

    private Logger getLogger() {
        return LOGGER;
    }
}
