// Created: 21.01.2018
package de.freese.spring.thymeleaf.config;

import javax.servlet.http.HttpServletRequest;

import org.springframework.security.web.authentication.preauth.RequestHeaderAuthenticationFilter;

/**
 * Pre-Authentication<br>
 *
 * @author Thomas Freese
 */
public class MyTokenRequestHeaderAuthenticationFilter extends RequestHeaderAuthenticationFilter
{
    /**
     * Erstellt ein neues {@link MyTokenRequestHeaderAuthenticationFilter} Object.
     */
    public MyTokenRequestHeaderAuthenticationFilter()
    {
        super();

        setPrincipalRequestHeader("my-token");

        setExceptionIfHeaderMissing(false); // Damit keine Fehlermeldung ausgegeben wird.
        setCheckForPrincipalChanges(true);
        setInvalidateSessionOnPrincipalChange(true);
    }

    /**
     * @see org.springframework.security.web.authentication.preauth.RequestHeaderAuthenticationFilter#getPreAuthenticatedCredentials(javax.servlet.http.HttpServletRequest)
     */
    @Override
    protected Object getPreAuthenticatedCredentials(final HttpServletRequest request)
    {
        // Decode Credentials
        return super.getPreAuthenticatedCredentials(request);
    }

    /**
     * @see org.springframework.security.web.authentication.preauth.RequestHeaderAuthenticationFilter#getPreAuthenticatedPrincipal(javax.servlet.http.HttpServletRequest)
     */
    @Override
    protected Object getPreAuthenticatedPrincipal(final HttpServletRequest request)
    {
        // Decode Principal
        return super.getPreAuthenticatedPrincipal(request);
    }
}
