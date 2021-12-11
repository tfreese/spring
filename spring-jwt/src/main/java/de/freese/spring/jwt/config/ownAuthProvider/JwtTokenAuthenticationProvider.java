// Created: 30.10.2018
package de.freese.spring.jwt.config.ownAuthProvider;

import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.Authentication;

import de.freese.spring.jwt.token.JwtTokenUtils;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;

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
    private JwtTokenUtils jwtTokenUtils;

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

        Jws<Claims> claims = getJwtTokenUtils().parseToken(token);

        String username = getJwtTokenUtils().getUsername(claims);
        String password = getJwtTokenUtils().getPassword(claims);

        return super.authenticate(new UsernamePasswordAuthenticationToken(username, password));
    }

    /**
     * @return {@link JwtTokenUtils}
     */
    protected JwtTokenUtils getJwtTokenUtils()
    {
        return this.jwtTokenUtils;
    }

    /**
     * @return {@link MessageSourceAccessor}
     */
    protected MessageSourceAccessor getMessages()
    {
        return super.messages;
    }

    /**
     * @param jwtTokenUtils {@link JwtTokenUtils}
     */
    public void setJwtTokenUtils(final JwtTokenUtils jwtTokenUtils)
    {
        this.jwtTokenUtils = jwtTokenUtils;
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
