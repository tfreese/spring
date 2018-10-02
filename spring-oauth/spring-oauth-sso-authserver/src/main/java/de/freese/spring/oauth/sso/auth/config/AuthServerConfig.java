package de.freese.spring.oauth.sso.auth.config;

import javax.annotation.Resource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.config.annotation.builders.InMemoryClientDetailsServiceBuilder;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.approval.ApprovalStore;
import org.springframework.security.oauth2.provider.approval.InMemoryApprovalStore;
import org.springframework.security.oauth2.provider.code.AuthorizationCodeServices;
import org.springframework.security.oauth2.provider.code.InMemoryAuthorizationCodeServices;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.InMemoryTokenStore;

/**
 * @author Thomas Freese
 */
@Configuration
@EnableAuthorizationServer
public class AuthServerConfig extends AuthorizationServerConfigurerAdapter
{
    // /**
    // *
    // */
    // @Resource
    // private AuthenticationManager authenticationManager = null;

    /**
     *
     */
    @Resource
    private PasswordEncoder passwordEncoder = null;

    // /**
    // *
    // */
    // @Resource
    // private UserDetailsService userDetailsService = null;

    /**
     * Erstellt ein neues {@link AuthServerConfig} Object.
     */
    public AuthServerConfig()
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
        // .authorizedGrantTypes("authorization_code", "client_credentials", "password", "refresh_token", "implicit")

        // @formatter:off
        return new InMemoryClientDetailsServiceBuilder()
                .withClient("my-client-id")
                    .secret(this.passwordEncoder.encode("secret"))
                    .authorizedGrantTypes("authorization_code")
                    .scopes("user_info") // .scopes("read", "write", "trust")
                    .autoApprove(true)
                    .redirectUris("http://localhost:8082/ui/login", "http://localhost:8083/ui2/login", "http://localhost:8082/login")
                    .accessTokenValiditySeconds(300) // 5 Minuten
                    .refreshTokenValiditySeconds(3600) // 1 Stunde
                .and()
                .build()
                ;
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
            //.authenticationManager(this.authenticationManager)
            //.userDetailsService(this.userDetailsService)
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

        // clients
        // .inMemory()
        // .withClient("SampleClientId")
        // .secret...
        // ;
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
