// Created: 21.01.2018
package de.freese.spring.thymeleaf.config;

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
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.NullRememberMeServices;
import org.springframework.security.web.authentication.RememberMeServices;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Pre-Authentication<br>
 * Implementierung analog {@link BasicAuthenticationFilter}.
 *
 * @author Thomas Freese
 */
public class MyTokenBasicAuthAuthenticationFilter extends OncePerRequestFilter {
    private static final Logger LOGGER = LoggerFactory.getLogger(MyTokenBasicAuthAuthenticationFilter.class);

    private final AuthenticationManager authenticationManager;

    private AuthenticationDetailsSource<HttpServletRequest, ?> authenticationDetailsSource = new WebAuthenticationDetailsSource();

    private AuthenticationEntryPoint authenticationEntryPoint;

    private boolean ignoreFailure;

    private RememberMeServices rememberMeServices = new NullRememberMeServices();

    public MyTokenBasicAuthAuthenticationFilter(final AuthenticationManager authenticationManager) {
        super();

        this.authenticationManager = Objects.requireNonNull(authenticationManager, "authenticationManager required");
        this.ignoreFailure = true;
    }

    public MyTokenBasicAuthAuthenticationFilter(final AuthenticationManager authenticationManager, final AuthenticationEntryPoint authenticationEntryPoint) {
        super();

        this.authenticationManager = Objects.requireNonNull(authenticationManager, "authenticationManager required");
        this.authenticationEntryPoint = Objects.requireNonNull(authenticationEntryPoint, "authenticationEntryPoint required");

        // setPrincipalRequestHeader("my-token");
        //
        // setExceptionIfHeaderMissing(false); // Damit keine Fehlermeldung ausgegeben wird.
        // setCheckForPrincipalChanges(true);
        // setInvalidateSessionOnPrincipalChange(true);
    }

    public void setAuthenticationDetailsSource(final AuthenticationDetailsSource<HttpServletRequest, ?> authenticationDetailsSource) {
        this.authenticationDetailsSource = Objects.requireNonNull(authenticationDetailsSource, "authenticationDetailsSource required");
    }

    public void setIgnoreFailure(final boolean ignoreFailure) {
        this.ignoreFailure = ignoreFailure;
    }

    public void setRememberMeServices(final RememberMeServices rememberMeServices) {
        this.rememberMeServices = Objects.requireNonNull(rememberMeServices, "rememberMeServices required");
    }

    @Override
    protected void doFilterInternal(final HttpServletRequest request, final HttpServletResponse response, final FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader("my-token");

        if ((header == null) || header.isEmpty()) {
            filterChain.doFilter(request, response);

            return;
        }

        try {
            // Decode Credentials.
            String username = header;
            String password = "pw";

            LOGGER.debug("MyToken Pre-Authentication Authorization header found for user '{}'", username);

            if (isAuthenticationIsRequired(username)) {
                // AbstractAuthenticationToken authRequest = new UsernamePasswordAuthenticationToken(username, password);
                AbstractAuthenticationToken authRequest = new PreAuthenticatedAuthenticationToken(username, password);
                authRequest.setDetails(this.authenticationDetailsSource.buildDetails(request));
                Authentication authResult = this.authenticationManager.authenticate(authRequest);
                // Authentication authResult = new PreAuthenticatedAuthenticationToken(username, null, Arrays.asList(new SimpleGrantedAuthority("ROLE_USER")));

                LOGGER.debug("Authentication success: {}", authResult);

                SecurityContextHolder.getContext().setAuthentication(authResult);

                this.rememberMeServices.loginSuccess(request, response, authResult);

                onSuccessfulAuthentication(request, response, authResult);
            }
        }
        catch (AuthenticationException failed) {
            SecurityContextHolder.clearContext();

            LOGGER.debug("Authentication request for failed: {}", failed.getMessage());

            this.rememberMeServices.loginFail(request, response);

            onUnsuccessfulAuthentication(request, response, failed);

            if (isIgnoreFailure()) {
                filterChain.doFilter(request, response);
            }
            else {
                this.authenticationEntryPoint.commence(request, response, failed);
            }

            return;
        }

        filterChain.doFilter(request, response);
    }

    protected boolean isAuthenticationIsRequired(final String username) {
        // Only reauthenticate if username doesn't match SecurityContextHolder and user
        // isn't authenticated (see SEC-53)
        Authentication existingAuth = SecurityContextHolder.getContext().getAuthentication();

        // Limit username comparison to providers which use usernames (ie
        // UsernamePasswordAuthenticationToken)
        // (see SEC-348)
        if ((existingAuth == null) || !existingAuth.isAuthenticated() || ((existingAuth instanceof UsernamePasswordAuthenticationToken) && !existingAuth.getName().equals(username))) {
            return true;
        }

        if ((existingAuth instanceof PreAuthenticatedAuthenticationToken) && !existingAuth.getName().equals(username)) {
            return true;
        }

        // Handle unusual condition where an AnonymousAuthenticationToken is already
        // present
        // This shouldn't happen very often, as BasicProcessingFilter is meant to be
        // earlier in the filter
        // chain than AnonymousAuthenticationFilter. Nevertheless, presence of both an
        // AnonymousAuthenticationToken
        // together with a BASIC authentication request header should indicate
        // reauthentication using the
        // BASIC protocol is desirable. This behaviour is also consistent with that
        // provided by form and digest,
        // both of which force re-authentication if the respective header is detected (and
        // in doing so replace
        // any existing AnonymousAuthenticationToken). See SEC-610.
        return existingAuth instanceof AnonymousAuthenticationToken;
    }

    protected boolean isIgnoreFailure() {
        return this.ignoreFailure;
    }

    protected void onSuccessfulAuthentication(final HttpServletRequest request, final HttpServletResponse response, final Authentication authResult) throws IOException {
        // Empty
    }

    protected void onUnsuccessfulAuthentication(final HttpServletRequest request, final HttpServletResponse response, final AuthenticationException failed) throws IOException {
        // Empty
    }
}
