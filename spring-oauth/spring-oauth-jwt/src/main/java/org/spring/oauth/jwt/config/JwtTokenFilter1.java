/**
 * Created: 28.10.2018
 */

package org.spring.oauth.jwt.config;

import java.io.IOException;
import java.util.Objects;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.spring.oauth.jwt.exception.MyJwtException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.CredentialsContainer;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.GenericFilterBean;

/**
 * @author Thomas Freese
 */
public class JwtTokenFilter1 extends GenericFilterBean
{
    /**
     *
     */
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * Erstellt ein neues {@link JwtTokenFilter1} Object.
     *
     * @param jwtTokenProvider {@link JwtTokenProvider}
     */
    public JwtTokenFilter1(final JwtTokenProvider jwtTokenProvider)
    {
        this.jwtTokenProvider = Objects.requireNonNull(jwtTokenProvider, "jwtTokenProvider required");
    }

    /**
     * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse, javax.servlet.FilterChain)
     */
    @Override
    public void doFilter(final ServletRequest req, final ServletResponse res, final FilterChain filterChain) throws IOException, ServletException
    {
        String token = this.jwtTokenProvider.resolveToken((HttpServletRequest) req);

        try
        {
            if ((token != null) && this.jwtTokenProvider.validateToken(token))
            {
                Authentication authRequest = this.jwtTokenProvider.getAuthentication(token);

                if (authRequest instanceof CredentialsContainer)
                {
                    ((CredentialsContainer) authRequest).eraseCredentials();
                }

                SecurityContextHolder.getContext().setAuthentication(authRequest);
            }
        }
        catch (MyJwtException ex)
        {
            HttpServletResponse response = (HttpServletResponse) res;
            response.sendError(ex.getHttpStatus().value(), ex.getMessage());

            return;
        }

        filterChain.doFilter(req, res);
    }
}