// Created: 30.10.2018
package de.freese.spring.jwt.config.defaultAuthProvider;

import java.io.IOException;
import java.util.Objects;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

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
import org.springframework.security.oauth2.server.resource.authentication.BearerTokenAuthenticationToken;
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;
import org.springframework.security.oauth2.server.resource.web.DefaultBearerTokenResolver;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * @author Thomas Freese
 * @see BearerTokenAuthenticationFilter
 */
class JwtRequestFilter extends OncePerRequestFilter {
    private static final Logger LOGGER = LoggerFactory.getLogger(JwtRequestFilter.class);

    private final AuthenticationDetailsSource<HttpServletRequest, ?> authenticationDetailsSource = new WebAuthenticationDetailsSource();
    private final AuthenticationEntryPoint authenticationEntryPoint;
    private final AuthenticationManager authenticationManager;
    private final BearerTokenResolver bearerTokenResolver = new DefaultBearerTokenResolver();

    JwtRequestFilter(final AuthenticationManager authenticationManager, final AuthenticationEntryPoint authenticationEntryPoint) {
        super();

        this.authenticationManager = authenticationManager;
        this.authenticationEntryPoint = authenticationEntryPoint;
    }

    @Override
    protected void doFilterInternal(final HttpServletRequest request, final HttpServletResponse response, final FilterChain filterChain) throws ServletException, IOException {
        try {
            final String bearerToken = bearerTokenResolver.resolve(request);

            final AbstractAuthenticationToken authenticationToken = new BearerTokenAuthenticationToken(bearerToken);
            authenticationToken.setDetails(this.authenticationDetailsSource.buildDetails(request));

            if (isAuthenticationIsRequired(authenticationToken.getName())) {
                final Authentication authResult = this.authenticationManager.authenticate(authenticationToken);

                SecurityContextHolder.getContext().setAuthentication(authResult);
                // SecurityContext context = SecurityContextHolder.createEmptyContext();
                // context.setAuthentication(authResult);
                // SecurityContextHolder.setContext(context);
            }
        }
        catch (AuthenticationException ex) {
            SecurityContextHolder.clearContext();

            LOGGER.error("Authentication request failed: {}", ex.getMessage());

            // // Deswegen würden Tests der Logins über den RestController nicht mehr funktionieren !
            this.authenticationEntryPoint.commence(request, response, ex);

            return;
        }

        filterChain.doFilter(request, response);
    }

    @Override
    protected void initFilterBean() throws ServletException {
        super.initFilterBean();

        Objects.requireNonNull(this.authenticationManager, "authenticationManager required");
        Objects.requireNonNull(this.authenticationEntryPoint, "authenticationEntryPoint required");
    }

    private boolean isAuthenticationIsRequired(final String username) {
        final Authentication existingAuth = SecurityContextHolder.getContext().getAuthentication();

        if ((existingAuth == null) || !existingAuth.isAuthenticated() ||
                ((existingAuth instanceof UsernamePasswordAuthenticationToken) && !existingAuth.getName().equals(username))) {
            return true;
        }

        return (existingAuth instanceof AnonymousAuthenticationToken);
    }
}
