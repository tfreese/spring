/**
 * Created: 22.09.2018
 */

package de.freese.spring.oauth2.authorisation.config;

import javax.annotation.Resource;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.approval.ApprovalStore;
import org.springframework.security.oauth2.provider.code.AuthorizationCodeServices;
import org.springframework.security.oauth2.provider.token.TokenStore;

/**
 * @author Thomas Freese
 */
@Configuration
@EnableAuthorizationServer
public class AuthorisationServerConfig extends AuthorizationServerConfigurerAdapter
{
    /**
    *
    */
    @Resource
    private ApprovalStore approvalStore = null;

    /**
     *
     */
    @Resource
    private AuthenticationManager authenticationManager = null;

    /**
    *
    */
    @Resource
    private AuthorizationCodeServices authorizationCodeServices = null;

    /**
       *
       */
    @Resource
    private ClientDetailsService myClientDetailsService = null;

    /**
     *
     */
    @Resource
    private PasswordEncoder passwordEncoder = null;

    /**
    *
    */
    @Resource
    private TokenStore tokenStore = null;

    /**
    *
    */
    @Resource
    private UserDetailsService userDetailsService = null;

    /**
     * Erstellt ein neues {@link AuthorisationServerConfig} Object.
     */
    public AuthorisationServerConfig()
    {
        super();
    }

    /**
     * @see org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter#configure(org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer)
     */
    @Override
    public void configure(final AuthorizationServerEndpointsConfigurer endpoints) throws Exception
    {
        // @formatter:off
        endpoints
            .approvalStore(this.approvalStore)
            .authorizationCodeServices(this.authorizationCodeServices)
            .tokenStore(this.tokenStore)
            .authenticationManager(this.authenticationManager)
            .userDetailsService(this.userDetailsService)
        ;
        // @formatter:on
    }

    /**
     * @see org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter#configure(org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer)
     */
    @Override
    public void configure(final AuthorizationServerSecurityConfigurer oauthServer) throws Exception
    {
        // @formatter:off
        oauthServer
            .tokenKeyAccess("permitAll()")
            .checkTokenAccess("isAuthenticated()")
            .passwordEncoder(this.passwordEncoder)
            .realm("my_realm")
        ;
        // @formatter:on
    }

    /**
     * @see org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter#configure(org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer)
     */
    @Override
    public void configure(final ClientDetailsServiceConfigurer clients) throws Exception
    {
        // @formatter:off
        clients
            .withClientDetails(this.myClientDetailsService)
            //.jdbc(this.dataSource)
        ;
        // @formatter:on
    }
}
