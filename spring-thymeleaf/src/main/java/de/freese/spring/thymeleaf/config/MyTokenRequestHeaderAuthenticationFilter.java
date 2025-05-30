// Created: 21.01.2018
package de.freese.spring.thymeleaf.config;

import org.springframework.security.web.authentication.preauth.RequestHeaderAuthenticationFilter;

/**
 * Pre-Authentication<br>
 *
 * @author Thomas Freese
 */
public final class MyTokenRequestHeaderAuthenticationFilter extends RequestHeaderAuthenticationFilter {
    public static RequestHeaderAuthenticationFilter of(final String principalRequestHeader) {
        final MyTokenRequestHeaderAuthenticationFilter filter = new MyTokenRequestHeaderAuthenticationFilter();
        filter.setPrincipalRequestHeader(principalRequestHeader);

        filter.setExceptionIfHeaderMissing(false); // No Exception.
        filter.setCheckForPrincipalChanges(true);
        filter.setInvalidateSessionOnPrincipalChange(true);

        return filter;
    }

    private MyTokenRequestHeaderAuthenticationFilter() {
        super();
    }

    // @Override
    // protected Object getPreAuthenticatedCredentials(final HttpServletRequest request) {
    //     // Decode Credentials
    //     return super.getPreAuthenticatedCredentials(request);
    // }

    // @Override
    // protected Object getPreAuthenticatedPrincipal(final HttpServletRequest request) {
    //     // Decode Principal
    //     return super.getPreAuthenticatedPrincipal(request);
    // }
}
