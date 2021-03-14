/**
 * Created: 07.11.2019
 */
package de.freese.spring.oauth2.authorisation.config;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.www.BasicAuthenticationEntryPoint;

/**
 * BasicAuthenticationEntryPoint liefert die volle HTML Fehler-Seite, dies ist bei REST nicht gewünscht.<br>
 * Aussedem wird die FilterChain weiter ausgeführt, wenn keine Credentials vorhanden sind.<br>
 *
 * <pre>
 * AuthorizationServerSecurityConfigurer.authenticationEntryPoint(new RestAuthenticationEntryPoint());
 * </pre>
 *
 * @author Thomas Freese
 */
public class RestAuthenticationEntryPoint extends BasicAuthenticationEntryPoint
{
    /**
     * @see org.springframework.security.web.authentication.www.BasicAuthenticationEntryPoint#afterPropertiesSet()
     */
    @Override
    public void afterPropertiesSet()
    {
        setRealmName("my_realm");

        super.afterPropertiesSet();
    }

    /**
     * @see org.springframework.security.web.authentication.www.BasicAuthenticationEntryPoint#commence(javax.servlet.http.HttpServletRequest,
     *      javax.servlet.http.HttpServletResponse, org.springframework.security.core.AuthenticationException)
     */
    @Override
    public void commence(final HttpServletRequest request, final HttpServletResponse response, final AuthenticationException authEx) throws IOException
    {
        response.addHeader("WWW-Authenticate", "Basic realm=" + getRealmName());
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        @SuppressWarnings("resource")
        PrintWriter writer = response.getWriter();
        writer.println("HTTP Status 401 - " + authEx.getMessage());
    }
}
