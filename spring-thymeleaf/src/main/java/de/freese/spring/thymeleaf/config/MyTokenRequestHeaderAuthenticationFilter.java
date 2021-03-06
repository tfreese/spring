/**
 * Created: 21.01.2018
 */
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
        String credentials = (String) super.getPreAuthenticatedCredentials(request);

        // Decode Credentials

        return credentials;
    }

    /**
     * @see org.springframework.security.web.authentication.preauth.RequestHeaderAuthenticationFilter#getPreAuthenticatedPrincipal(javax.servlet.http.HttpServletRequest)
     */
    @Override
    protected Object getPreAuthenticatedPrincipal(final HttpServletRequest request)
    {
        String principal = (String) super.getPreAuthenticatedPrincipal(request);

        // Decode Principal

        return principal;
    }
}
