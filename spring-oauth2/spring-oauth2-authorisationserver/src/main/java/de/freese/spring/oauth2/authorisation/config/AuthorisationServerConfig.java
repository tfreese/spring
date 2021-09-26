// Created: 22.09.2018
package de.freese.spring.oauth2.authorisation.config;

import java.util.Arrays;

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
import org.springframework.security.oauth2.provider.token.AccessTokenConverter;
import org.springframework.security.oauth2.provider.token.TokenEnhancer;
import org.springframework.security.oauth2.provider.token.TokenEnhancerChain;
import org.springframework.security.oauth2.provider.token.TokenStore;

/**
 * Der AuthorisationServer verwaltet sämtliche Berechtigungen und verarbeitet alle Token-Anfragen.
 *
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
    private AccessTokenConverter accessTokenConverter;
    /**
    *
    */
    @Resource
    private ApprovalStore approvalStore;
    /**
     *
     */
    @Resource
    private AuthenticationManager authenticationManager;
    /**
    *
    */
    @Resource
    private AuthorizationCodeServices authorizationCodeServices;
    /**
    *
    */
    @Resource
    private ClientDetailsService myClientDetailsService;
    /**
     *
     */
    @Resource
    private PasswordEncoder passwordEncoder;
    /**
     *
     */
    @Resource
    private TokenEnhancer tokenEnhancer;
    /**
    *
    */
    @Resource
    private TokenStore tokenStore;
    /**
    *
    */
    @Resource
    private UserDetailsService userDetailsService;

    /**
     * @see org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter#configure(org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer)
     */
    @Override
    public void configure(final AuthorizationServerEndpointsConfigurer endpoints) throws Exception
    {
        final TokenEnhancerChain tokenEnhancerChain = new TokenEnhancerChain();
        tokenEnhancerChain.setTokenEnhancers(Arrays.asList(this.tokenEnhancer));
        // tokenEnhancerChain.setTokenEnhancers(Arrays.asList(tokenEnhancer, JwtAccessTokenConverter)); // Kein .accessTokenConverter(...) notwendig !

        // @formatter:off
        endpoints
            .accessTokenConverter(this.accessTokenConverter)
            .approvalStore(this.approvalStore)
            .authenticationManager(this.authenticationManager)
            .authorizationCodeServices(this.authorizationCodeServices)
//            .tokenEnhancer(tokenEnhancerChain)
            .tokenStore(this.tokenStore)
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
//            .passwordEncoder(this.passwordEncoder)
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
