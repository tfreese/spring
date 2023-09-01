// Created: 21.01.2018
package de.freese.spring.thymeleaf.config;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.security.web.authentication.preauth.RequestHeaderAuthenticationFilter;

/**
 * Pre-Authentication<br>
 *
 * @author Thomas Freese
 */
public class MyTokenRequestHeaderAuthenticationFilter extends RequestHeaderAuthenticationFilter {
    public MyTokenRequestHeaderAuthenticationFilter() {
        super();

        setPrincipalRequestHeader("my-token");

        setExceptionIfHeaderMissing(false); // Damit keine Fehlermeldung ausgegeben wird.
        setCheckForPrincipalChanges(true);
        setInvalidateSessionOnPrincipalChange(true);
    }

    @Override
    protected Object getPreAuthenticatedCredentials(final HttpServletRequest request) {
        // Decode Credentials
        return super.getPreAuthenticatedCredentials(request);
    }

    @Override
    protected Object getPreAuthenticatedPrincipal(final HttpServletRequest request) {
        // Decode Principal
        return super.getPreAuthenticatedPrincipal(request);
    }
}
