/**
 * Created: 22.09.2018
 */

package org.spring.oauth.jwt.config;

import javax.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
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
import org.springframework.security.oauth2.provider.token.AccessTokenConverter;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;
import org.springframework.security.web.AuthenticationEntryPoint;

/**
 * Die HttpSecurity wird über die Annotation {@link EnableAuthorizationServer} in {@link AuthorizationServerSecurityConfiguration} gemacht.<br>
 * Für die User-Passwörter benötigt der Authorisation-Server einen {@link AuthenticationManager}, {@link UserDetailsService} und {@link PasswordEncoder}.<br>
 * Diese werden in der {@link SecurityConfig} erzeugt.
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
    private AuthenticationEntryPoint authenticationEntryPoint = null;

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
    @Value("${security.jwt.token.secret-key:secret-key}")
    private String signingKey = null;

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
     * @return {@link AccessTokenConverter}
     */
    @Bean
    public JwtAccessTokenConverter accessTokenConverter()
    {
        JwtAccessTokenConverter converter = new JwtAccessTokenConverter();
        converter.setSigningKey(this.signingKey);

        // Für Zertifikate statt SigningKey.
        // converter.setKeyPair(keyPair);

        return converter;
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
            .accessTokenConverter(accessTokenConverter())
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
            .authenticationEntryPoint(this.authenticationEntryPoint)
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
            .withClientDetails(myClientDetailsService())
        ;
        // @formatter:on
    }

    /**
     * @return {@link ClientDetailsService}
     * @throws Exception Falls was schief geht.
     */
    @Bean
    public ClientDetailsService myClientDetailsService() throws Exception
    {
        // @formatter:off
        return new InMemoryClientDetailsServiceBuilder()
                .withClient("my-client-id-read")
                    .resourceIds("my-oauth-app")
                    .secret("{noop}my-secret")
                    .authorizedGrantTypes("authorization_code", "client_credentials", "password", "refresh_token", "implicit")
                    .authorities("USER")
                    .scopes("user_info", "read")
                    .autoApprove(true)
                    .accessTokenValiditySeconds(120) // 2 Minuten
                    .refreshTokenValiditySeconds(3600) // 1 Stunde
                .and()
                .withClient("my-client-id-write")
                    .resourceIds("my-oauth-app")
                    .secret("{noop}my-secret")
                    .authorizedGrantTypes("authorization_code", "client_credentials", "password", "refresh_token", "implicit")
                    .authorities("USER", "ADMIN")
                    .scopes("user_info", "read", "write")
                    .autoApprove(true)
                    .accessTokenValiditySeconds(120) // 2 Minuten
                    .refreshTokenValiditySeconds(3600) // 1 Stunde
                .and()
                .build()
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
     * @return {@link DefaultTokenServices}
     */
    @Bean
    // @Primary
    public DefaultTokenServices tokenServices()
    {
        DefaultTokenServices defaultTokenServices = new DefaultTokenServices();
        defaultTokenServices.setTokenStore(tokenStore());
        defaultTokenServices.setSupportRefreshToken(true);

        return defaultTokenServices;
    }

    /**
     * @return {@link TokenStore}
     */
    @Bean
    public TokenStore tokenStore()
    {
        // return new InMemoryTokenStore();
        return new JwtTokenStore(accessTokenConverter());
    }
}