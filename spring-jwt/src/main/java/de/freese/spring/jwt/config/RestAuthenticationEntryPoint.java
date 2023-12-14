// Created: 08.12.2021
package de.freese.spring.jwt.config;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.www.BasicAuthenticationEntryPoint;

/**
 * BasicAuthenticationEntryPoint liefert die volle HTML Fehler-Seite, dies ist bei REST nicht gewünscht.<br>
 * Ausserdem wird die FilterChain weiter ausgeführt, wenn keine Credentials vorhanden sind.<br>
 *
 * @author Thomas Freese
 */
class RestAuthenticationEntryPoint extends BasicAuthenticationEntryPoint {
    @Override
    public void afterPropertiesSet() {
        setRealmName("Tommy");

        super.afterPropertiesSet();
    }

    @Override
    public void commence(final HttpServletRequest request, final HttpServletResponse response, final AuthenticationException authEx) throws IOException {
        response.addHeader("WWW-Authenticate", "Basic realm=" + getRealmName());
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());

        final PrintWriter writer = response.getWriter();
        writer.println("HTTP Status 401 - " + authEx.getMessage());
        writer.flush();

        // response.sendError(HttpStatus.UNAUTHORIZED.value(), authEx.getMessage());
    }
}
