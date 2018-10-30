/**
 * Created: 30.10.2018
 */

package org.spring.oauth.jwt.token;

import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceAware;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.security.authentication.AccountStatusUserDetailsChecker;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.SpringSecurityMessageSource;
import org.springframework.security.core.userdetails.UserCache;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsChecker;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.core.userdetails.cache.NullUserCache;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * @author Thomas Freese
 */
public class JwtTokenAuthenticationProvider implements AuthenticationProvider, InitializingBean, MessageSourceAware
{
    /**
    *
    */
    private static final Logger LOGGER = LoggerFactory.getLogger(JwtTokenAuthenticationProvider.class);

    /**
     *
     */
    private MessageSourceAccessor messages = SpringSecurityMessageSource.getAccessor();

    /**
    *
    */
    private PasswordEncoder passwordEncoder = null;

    /**
     *
     */
    private JwtTokenProvider tokenProvider = null;

    /**
     *
     */
    private UserCache userCache = new NullUserCache();

    /**
     *
     */
    private UserDetailsChecker userDetailsChecker = new AccountStatusUserDetailsChecker();

    /**
     *
     */
    private UserDetailsService userDetailsService = null;

    /**
     * Erstellt ein neues {@link JwtTokenAuthenticationProvider} Object.
     */
    public JwtTokenAuthenticationProvider()
    {
        super();
    }

    /**
     * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
     */
    @Override
    public void afterPropertiesSet() throws Exception
    {
        Objects.requireNonNull(this.userDetailsChecker, "userDetailsChecker requried");
        Objects.requireNonNull(this.userDetailsService, "userDetailsService requried");
        Objects.requireNonNull(this.userCache, "userCache requried");
        Objects.requireNonNull(this.tokenProvider, "tokenProvider requried");
        Objects.requireNonNull(this.messages, "messageSource requried");
        Objects.requireNonNull(this.passwordEncoder, "passwordEncoder requried");
    }

    /**
     * @see org.springframework.security.authentication.AuthenticationProvider#authenticate(org.springframework.security.core.Authentication)
     */
    @Override
    public Authentication authenticate(final Authentication authentication) throws AuthenticationException
    {
        // if (!supports(authentication.getClass())) {
        // return null;
        // }

        if (!(authentication instanceof JwtAuthenticationToken))
        {
            String message = this.messages.getMessage("JwtTokenAuthenticationProvider.onlySupports",
                    "JwtTokenAuthenticationProvider only supports JwtAuthenticationToken");

            throw new IllegalArgumentException(message);
        }

        if (getLogger().isDebugEnabled())
        {
            getLogger().debug("JwtToken authentication request: {}", authentication);
        }

        JwtAuthenticationToken jwtAuthentication = (JwtAuthenticationToken) authentication;
        String token = jwtAuthentication.getToken();

        if ((token == null) || token.isBlank() || !this.tokenProvider.validateToken(token))
        {
            String message = this.messages.getMessage("JwtTokenAuthenticationProvider.token.invalid", "JwtToken is invalid");

            throw new AuthenticationServiceException(message);
        }

        // TODO JWT Expiry Check

        String userName = this.tokenProvider.getUsername(token);
        String password = this.tokenProvider.getPassword(token);

        UserDetails userDetails = this.userCache.getUserFromCache(userName);

        if (userDetails == null)
        {
            try
            {
                userDetails = this.userDetailsService.loadUserByUsername(userName);
                this.userCache.putUserInCache(userDetails);
            }
            catch (UsernameNotFoundException unfex)
            {
                getLogger().debug("User '{}' not found", userName);

                String message = this.messages.getMessage("JwtTokenAuthenticationProvider.user.notFound", "Bad credentials");

                throw new BadCredentialsException(message);
            }
        }

        boolean badCredentials = false;

        try
        {
            badCredentials = !this.passwordEncoder.matches(password, userDetails.getPassword());
        }
        catch (Exception ex)
        {
            // Falsch formatiertes Password: Soll = {PREFIX}PASSWORD
            badCredentials = true;
        }

        if (badCredentials)
        {
            getLogger().debug("Bad credentials");

            String message = this.messages.getMessage("JwtTokenAuthenticationProvider.credentials.bad", "Bad credentials");

            throw new BadCredentialsException(message);
        }

        // TODO UserDetailsChecker

        JwtAuthenticationToken authenticationToken = new JwtAuthenticationToken(userDetails, "", userDetails.getAuthorities());
        authenticationToken.setAuthenticated(true);

        return authenticationToken;
    }

    /**
     * @return {@link Logger}
     */
    protected Logger getLogger()
    {
        return LOGGER;
    }

    /**
     * @see org.springframework.context.MessageSourceAware#setMessageSource(org.springframework.context.MessageSource)
     */
    @Override
    public void setMessageSource(final MessageSource messageSource)
    {
        this.messages = new MessageSourceAccessor(messageSource);
    }

    /**
     * @param passwordEncoder {@link PasswordEncoder}
     */
    public void setPasswordEncoder(final PasswordEncoder passwordEncoder)
    {
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * @param tokenProvider {@link JwtTokenProvider}
     */
    public void setTokenProvider(final JwtTokenProvider tokenProvider)
    {
        this.tokenProvider = tokenProvider;
    }

    /**
     * @param userCache {@link UserCache}
     */
    public void setUserCache(final UserCache userCache)
    {
        this.userCache = userCache;
    }

    /**
     * @param userDetailsChecker {@link UserDetailsChecker}
     */
    public void setUserDetailsChecker(final UserDetailsChecker userDetailsChecker)
    {
        this.userDetailsChecker = userDetailsChecker;
    }

    /**
     * @param userDetailsService {@link UserDetailsService}
     */
    public void setUserDetailsService(final UserDetailsService userDetailsService)
    {
        this.userDetailsService = userDetailsService;
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
