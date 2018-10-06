/**
 * Created: 22.09.2018
 */

package de.freese.spring.oauth.authorisation.config;

import javax.annotation.Resource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.config.annotation.builders.InMemoryClientDetailsServiceBuilder;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerSecurityConfiguration;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.approval.ApprovalStore;
import org.springframework.security.oauth2.provider.approval.InMemoryApprovalStore;
import org.springframework.security.oauth2.provider.code.AuthorizationCodeServices;
import org.springframework.security.oauth2.provider.code.InMemoryAuthorizationCodeServices;
import org.springframework.security.oauth2.provider.error.OAuth2AccessDeniedHandler;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.InMemoryTokenStore;

/**
 * Die HttpSecurity wird über die Annotation {@link EnableAuthorizationServer} in {@link AuthorizationServerSecurityConfiguration} gemacht.<br>
 * Für die User-Passwörter benötigt der Authorisation-Server einen {@link AuthenticationManager}, {@link UserDetailsService} und {@link PasswordEncoder}.<br>
 * Diese werden in der {@link SecurityConfig} erzeugt.
 *
 * @author Thomas Freese
 */
@Configuration
@EnableAuthorizationServer
public class OauthAuthorisationConfig extends AuthorizationServerConfigurerAdapter
{
    /**
     *
     */
    @Resource
    private AuthenticationManager authenticationManager = null;

    /**
     *
     */
    @Resource
    private PasswordEncoder passwordEncoder = null;

    /**
     *
     */
    @Resource
    private UserDetailsService userDetailsService = null;

    /**
     * Erstellt ein neues {@link OauthAuthorisationConfig} Object.
     */
    public OauthAuthorisationConfig()
    {
        super();
    }

    /**
     * @return {@link ApprovalStore}
     */
    @Bean
    public ApprovalStore approvalStore()
    {
        return new InMemoryApprovalStore();
    }

    /**
     * @return {@link AuthorizationCodeServices}
     */
    @Bean
    public AuthorizationCodeServices authorizationCodeServices()
    {
        return new InMemoryAuthorizationCodeServices();
    }

    /**
     * @return {@link ClientDetailsService}
     * @throws Exception Falls was schief geht.
     */
    @Bean
    public ClientDetailsService clientDetailsService() throws Exception
    {
        // @formatter:off
        return new InMemoryClientDetailsServiceBuilder()
                .withClient("my-client-id")
                    .secret(this.passwordEncoder.encode("{noop}my-secret"))
                    //.secret("my-secret")
                    .authorizedGrantTypes("authorization_code", "client_credentials", "password", "refresh_token", "implicit")
                    .scopes("user_info") // .scopes("read", "write", "trust")
                    .autoApprove(true)
                    .accessTokenValiditySeconds(120) // 2 Minuten
                    .refreshTokenValiditySeconds(3600) // 1 Stunde
                .and()
                .build();
        // @formatter:on
    }

    /**
     * @see org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter#configure(org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer)
     */
    @Override
    public void configure(final AuthorizationServerEndpointsConfigurer endpoints) throws Exception
    {
        // @formatter:off
        endpoints
            .approvalStore(approvalStore())
            .authorizationCodeServices(authorizationCodeServices())
            .tokenStore(tokenStore())
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
            .withClientDetails(clientDetailsService())
            ;
        // @formatter:on
    }

    /**
     * @return {@link OAuth2AccessDeniedHandler}
     */
    @Bean
    public OAuth2AccessDeniedHandler oauthAccessDeniedHandler()
    {
        return new OAuth2AccessDeniedHandler();
    }

    /**
     * @return {@link TokenStore}
     */
    @Bean
    public TokenStore tokenStore()
    {
        return new InMemoryTokenStore();
    }
}
