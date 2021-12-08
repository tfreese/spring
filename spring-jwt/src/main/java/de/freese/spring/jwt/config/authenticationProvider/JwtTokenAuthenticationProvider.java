// Created: 30.10.2018
package de.freese.spring.jwt.config.authenticationProvider;

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
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.SpringSecurityMessageSource;
import org.springframework.security.core.userdetails.UserCache;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsChecker;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.core.userdetails.cache.NullUserCache;
import org.springframework.security.crypto.password.PasswordEncoder;

import de.freese.spring.jwt.model.MutableUser;
import de.freese.spring.jwt.token.JwtTokenUtils;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.SignatureException;

/**
 * Analog-implementierung zum {@link DaoAuthenticationProvider}.
 *
 * @author Thomas Freese
 */
class JwtTokenAuthenticationProvider implements AuthenticationProvider, InitializingBean, MessageSourceAware
{
    /**
    *
    */
    private static final Logger LOGGER = LoggerFactory.getLogger(JwtTokenAuthenticationProvider.class);
    /**
     * The plaintext password used to perform PasswordEncoder#matches(CharSequence, String)} on when the user is not found to avoid SEC-2056.
     */
    private static final String USER_NOT_FOUND_PASSWORD = "userNotFoundPassword";
    /**
     *
     */
    private JwtTokenUtils jwtTokenUtils;
    /**
     *
     */
    private MessageSourceAccessor messages = SpringSecurityMessageSource.getAccessor();
    /**
    *
    */
    private PasswordEncoder passwordEncoder;
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
    private UserDetailsService userDetailsService;
    /**
     * The password used to perform {@link PasswordEncoder#matches(CharSequence, String)} on when the user is not found to avoid SEC-2056. This is necessary,
     * because some {@link PasswordEncoder} implementations will short circuit if the password is not in a valid format.
     */
    private volatile String userNotFoundEncodedPassword;

    /**
     * @param userDetails {@link UserDetails}
     * @param authentication {@link Authentication}
     * @param claims {@link Jws}
     */
    protected void additionalAuthenticationChecks(final UserDetails userDetails, final Authentication authentication, final Jws<Claims> claims)
    {
        String password = getJwtTokenUtils().getPassword(claims);

        // try
        // {
        if (!getPasswordEncoder().matches(password, userDetails.getPassword()))
        {
            getLogger().debug("Bad credentials: password does not match stored value");

            String message = getMessages().getMessage("JwtTokenAuthenticationProvider.credentials.bad", "Bad credentials");

            throw new BadCredentialsException(message);
        }
        // }
        // catch (Exception ex)
        // {
        // getLogger().debug("Bad credentials", ex);
        //
        // String message = getMessages().getMessage("JwtTokenAuthenticationProvider.credentials.bad", "Bad credentials");
        //
        // throw new BadCredentialsException(message);
        // }
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
        Objects.requireNonNull(this.jwtTokenUtils, "jwtTokenUtils requried");
        Objects.requireNonNull(this.messages, "messageSource requried");
        Objects.requireNonNull(this.passwordEncoder, "passwordEncoder requried");
    }

    /**
     * @see org.springframework.security.authentication.AuthenticationProvider#authenticate(org.springframework.security.core.Authentication)
     */
    @Override
    public Authentication authenticate(final Authentication authentication)
    {
        // if (!supports(authentication.getClass())) {
        // return null;
        // }

        if (!(authentication instanceof JwtAuthenticationToken))
        {
            String message = getMessages().getMessage("JwtTokenAuthenticationProvider.onlySupports",
                    "JwtTokenAuthenticationProvider only supports JwtAuthenticationToken");

            throw new IllegalArgumentException(message);
        }

        if (getLogger().isDebugEnabled())
        {
            getLogger().debug("JwtToken authentication request: {}", authentication);
        }

        JwtAuthenticationToken jwtAuthentication = (JwtAuthenticationToken) authentication;
        String token = jwtAuthentication.getToken();

        Jws<Claims> claims = null;

        try
        {
            claims = getJwtTokenUtils().parseToken(token);
        }
        catch (IllegalArgumentException ex)
        {
            getLogger().error("Unable to get JWT Token", ex);

            throw new AuthenticationServiceException("Unable to get JWT Token");
        }
        catch (ExpiredJwtException ex)
        {
            String message = getMessages().getMessage("JwtTokenAuthenticationProvider.token.expired", "JwtToken is expired");

            getLogger().error(message, ex);

            throw new AuthenticationServiceException(message);
        }
        catch (SignatureException ex)
        {
            getLogger().error("Authentication Failed. Username or Password not valid", ex);

            throw new AuthenticationServiceException("Authentication Failed. Username or Password not valid");
        }
        catch (JwtException ex)
        {
            String message = getMessages().getMessage("JwtTokenAuthenticationProvider.token.invalid", "JwtToken is invalid");

            throw new AuthenticationServiceException(message);
        }

        // Wird bereits im JwtTokenUtils.parseToken erledigt.
        // if (getJwtTokenUtils().getExpirationDate(claims).before(new Date()))
        // {
        // String message = getMessages().getMessage("JwtTokenAuthenticationProvider.token.expired", "JwtToken is expired");
        //
        // throw new AuthenticationServiceException(message);
        // }

        String username = getJwtTokenUtils().getUsername(claims);

        UserDetails userDetails = getUserCache().getUserFromCache(username);

        if (userDetails == null)
        {
            try
            {
                userDetails = retrieveUser(username, jwtAuthentication);
                getUserCache().putUserInCache(userDetails);
            }
            catch (UsernameNotFoundException unfex)
            {
                getLogger().debug("User '{}' not found", username);

                String message = getMessages().getMessage("JwtTokenAuthenticationProvider.user.notFound", "Bad credentials");

                throw new BadCredentialsException(message);
            }
        }

        // Durch AuthenticationManagerBuilder#eraseCredentials(true) würden die Passwörter aus den UserDetails entfernt.
        // Der UserCache würde dann leere Passwörter enthalten, wodurch der PasswordEncoder Fehler wirft.
        userDetails = new MutableUser(userDetails);

        additionalAuthenticationChecks(userDetails, jwtAuthentication, claims);

        getUserDetailsChecker().check(userDetails);

        JwtAuthenticationToken authenticationToken = new JwtAuthenticationToken(userDetails, userDetails.getPassword(), userDetails.getAuthorities());
        authenticationToken.setAuthenticated(true);

        return authenticationToken;
    }

    /**
     * @return {@link JwtTokenUtils}
     */
    protected JwtTokenUtils getJwtTokenUtils()
    {
        return this.jwtTokenUtils;
    }

    /**
     * @return {@link Logger}
     */
    protected Logger getLogger()
    {
        return LOGGER;
    }

    /**
     * @return {@link MessageSourceAccessor}
     */
    protected MessageSourceAccessor getMessages()
    {
        return this.messages;
    }

    /**
     * @return {@link PasswordEncoder}
     */
    protected PasswordEncoder getPasswordEncoder()
    {
        return this.passwordEncoder;
    }

    /**
     * @return {@link UserCache}
     */
    protected UserCache getUserCache()
    {
        return this.userCache;
    }

    /**
     * @return {@link UserDetailsChecker}
     */
    protected UserDetailsChecker getUserDetailsChecker()
    {
        return this.userDetailsChecker;
    }

    /**
     * @return {@link UserDetailsService}
     */
    protected UserDetailsService getUserDetailsService()
    {
        return this.userDetailsService;
    }

    /**
     * @param authentication {@link Authentication}
     */
    private void mitigateAgainstTimingAttack(final Authentication authentication)
    {
        if (authentication.getCredentials() != null)
        {
            String presentedPassword = authentication.getCredentials().toString();
            getPasswordEncoder().matches(presentedPassword, this.userNotFoundEncodedPassword);
        }
    }

    /**
     *
     */
    private void prepareTimingAttackProtection()
    {
        if (this.userNotFoundEncodedPassword == null)
        {
            this.userNotFoundEncodedPassword = getPasswordEncoder().encode(USER_NOT_FOUND_PASSWORD);
        }
    }

    /**
     * @param username String
     * @param authentication {@link Authentication}
     *
     * @return {@link UserDetails}
     */
    protected UserDetails retrieveUser(final String username, final Authentication authentication)
    {
        prepareTimingAttackProtection();

        try
        {
            UserDetails loadedUser = getUserDetailsService().loadUserByUsername(username);

            if (loadedUser == null)
            {
                throw new InternalAuthenticationServiceException("UserDetailsService returned null, which is an interface contract violation");
            }

            return loadedUser;
        }
        catch (UsernameNotFoundException ex)
        {
            mitigateAgainstTimingAttack(authentication);
            throw ex;
        }
        catch (InternalAuthenticationServiceException ex)
        {
            throw ex;
        }
        catch (Exception ex)
        {
            throw new InternalAuthenticationServiceException(ex.getMessage(), ex);
        }
    }

    /**
     * @param jwtTokenUtils {@link JwtTokenUtils}
     */
    public void setJwtTokenUtils(final JwtTokenUtils jwtTokenUtils)
    {
        this.jwtTokenUtils = jwtTokenUtils;
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
