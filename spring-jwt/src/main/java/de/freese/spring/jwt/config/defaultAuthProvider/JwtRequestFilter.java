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
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationDetailsSource;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.web.filter.OncePerRequestFilter;

import de.freese.spring.jwt.token.JwtToken;
import de.freese.spring.jwt.token.JwtTokenProvider;

/**
 * Der {@link JwtRequestFilter} verwendet den Default-{@link AuthenticationProvider}.<br>
 * Siehe {@link DaoAuthenticationProvider}.
 *
 * @author Thomas Freese
 * @see BasicAuthenticationFilter
 */
class JwtRequestFilter extends OncePerRequestFilter {
    private static final Logger LOGGER = LoggerFactory.getLogger(JwtRequestFilter.class);

    private final AuthenticationDetailsSource<HttpServletRequest, ?> authenticationDetailsSource = new WebAuthenticationDetailsSource();

    private AuthenticationEntryPoint authenticationEntryPoint;

    private AuthenticationManager authenticationManager;

    private JwtTokenProvider jwtTokenProvider;

    public void setAuthenticationEntryPoint(final AuthenticationEntryPoint authenticationEntryPoint) {
        this.authenticationEntryPoint = authenticationEntryPoint;
    }

    public void setAuthenticationManager(final AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

    public void setJwtTokenProvider(final JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    /**
     * @see org.springframework.web.filter.OncePerRequestFilter#doFilterInternal(jakarta.servlet.http.HttpServletRequest, jakarta.servlet.http.HttpServletResponse,
     * jakarta.servlet.FilterChain)
     */
    @Override
    protected void doFilterInternal(final HttpServletRequest request, final HttpServletResponse response, final FilterChain filterChain) throws ServletException, IOException {
        String token = this.jwtTokenProvider.resolveToken(request);

        try {
            String username = null;
            String password = null;

            if (token != null) {
                JwtToken jwtToken = this.jwtTokenProvider.parseToken(token);

                username = jwtToken.getUsername();
                password = jwtToken.getPassword();
            }

            if ((username != null) && isAuthenticationIsRequired(username)) {
                UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(username, password);
                usernamePasswordAuthenticationToken.setDetails(this.authenticationDetailsSource.buildDetails(request));

                Authentication authResult = this.authenticationManager.authenticate(usernamePasswordAuthenticationToken);

                SecurityContextHolder.getContext().setAuthentication(authResult);
                // SecurityContext context = SecurityContextHolder.createEmptyContext();
                // context.setAuthentication(authResult);
                // SecurityContextHolder.setContext(context);
            }
        }
        catch (AuthenticationException ex) {
            SecurityContextHolder.clearContext();

            getLogger().error("Authentication request failed: {}", ex.getMessage());

            // // Deswegen würden Tests der Logins über den RestController nicht mehr funktionieren !
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
        Objects.requireNonNull(this.jwtTokenProvider, "jwtTokenProvider required");
    }

    private Logger getLogger() {
        return LOGGER;
    }

    private boolean isAuthenticationIsRequired(final String username) {
        Authentication existingAuth = SecurityContextHolder.getContext().getAuthentication();

        if ((existingAuth == null) || !existingAuth.isAuthenticated() || ((existingAuth instanceof UsernamePasswordAuthenticationToken) && !existingAuth.getName().equals(username))) {
            return true;
        }

        return (existingAuth instanceof AnonymousAuthenticationToken);
    }
}
