// Created: 30.10.2018
package de.freese.spring.jwt.config.ownAuthProvider;

import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.Authentication;

import de.freese.spring.jwt.token.JwtToken;
import de.freese.spring.jwt.token.JwtTokenProvider;

/**
 * Analog-implementierung zum {@link DaoAuthenticationProvider}.
 *
 * @author Thomas Freese
 */
class JwtTokenAuthenticationProvider extends DaoAuthenticationProvider
{
    /**
     *
     */
    private JwtTokenProvider jwtTokenProvider;

    /**
     * @see org.springframework.security.authentication.AuthenticationProvider#authenticate(org.springframework.security.core.Authentication)
     */
    @Override
    public Authentication authenticate(final Authentication authentication)
    {
        if (!(authentication instanceof JwtAuthenticationToken jwtAuthentication))
        {
            String message = getMessages().getMessage("JwtTokenAuthenticationProvider.onlySupports",
                    "JwtTokenAuthenticationProvider only supports JwtAuthenticationToken");

            throw new IllegalArgumentException(message);
        }

        String token = jwtAuthentication.getToken();

        JwtToken jwtToken = getJwtTokenProvider().parseToken(token);

        String username = jwtToken.getUsername();
        String password = jwtToken.getPassword();

        return super.authenticate(new UsernamePasswordAuthenticationToken(username, password));
    }

    /**
     * @return {@link JwtTokenProvider}
     */
    protected JwtTokenProvider getJwtTokenProvider()
    {
        return this.jwtTokenProvider;
    }

    /**
     * @return {@link MessageSourceAccessor}
     */
    protected MessageSourceAccessor getMessages()
    {
        return super.messages;
    }

    /**
     * @param jwtTokenProvider {@link JwtTokenProvider}
     */
    public void setJwtTokenProvider(final JwtTokenProvider jwtTokenProvider)
    {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    /**
     * @see org.springframework.security.authentication.AuthenticationProvider#supports(java.lang.Class)
     */
    @Override
    public boolean supports(final Class<?> authentication)
    {
        return JwtAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
