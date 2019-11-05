/**
 * Created: 30.10.2018
 */

package de.freese.spring.oauth2.authorisation.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserCache;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.config.annotation.builders.InMemoryClientDetailsServiceBuilder;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.approval.ApprovalStore;
import org.springframework.security.oauth2.provider.approval.InMemoryApprovalStore;
import org.springframework.security.oauth2.provider.code.AuthorizationCodeServices;
import org.springframework.security.oauth2.provider.code.InMemoryAuthorizationCodeServices;
import org.springframework.security.oauth2.provider.token.AccessTokenConverter;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;

/**
 * @author Thomas Freese
 */
@Configuration
@Profile("jwt")
public class JwtConfig
{
    /**
     * Erstellt ein neues {@link JwtConfig} Object.
     */
    public JwtConfig()
    {
        super();
    }

    /**
     * @return {@link AccessTokenConverter}
     */
    @Bean
    public AccessTokenConverter accessTokenConverter()
    {
        JwtAccessTokenConverter converter = new JwtAccessTokenConverter();
        converter.setSigningKey("gehaim");

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
     * @param passwordEncoder {@link PasswordEncoder}
     * @return {@link ClientDetailsService}
     * @throws Exception Falls was schief geht.
     */
    @Bean
    public ClientDetailsService myClientDetailsService(final PasswordEncoder passwordEncoder) throws Exception
    {
        // @formatter:off
        return new InMemoryClientDetailsServiceBuilder()
                .withClient("my-app")
                    .resourceIds("my-app")
                    .secret(passwordEncoder.encode("app-secret"))
                    .scopes("user_info", "read", "write")
                    .authorizedGrantTypes("authorization_code", "client_credentials", "password", "refresh_token", "implicit")
                    .authorities("ROLE_ADMIN", "ROLE_USER")
                    .redirectUris("http://localhost:8888/login/oauth2/code/")
                    .accessTokenValiditySeconds(120) // 2 Minuten
                    .refreshTokenValiditySeconds(3600) // 1 Stunde
                    .additionalInformation("description:my oauth app")
                    .autoApprove(true)
                .and()
//                .withClient("...")
//                .and()
                .build()
        ;
        // @formatter:on
    }

    /**
     * @param passwordEncoder {@link PasswordEncoder}
     * @param userCache {@link UserCache}
     * @return {@link UserDetailsService}
     */
    @Bean
    public UserDetailsService myUserDetailsService(final PasswordEncoder passwordEncoder, final UserCache userCache)
    {
        // User.roles("USER",...) -> Authorities erhalten Prefix 'ROLE_' -> analog authorities("ROLE_USER")

        InMemoryUserDetailsManager userDetailsManager = new InMemoryUserDetailsManager();
        userDetailsManager.createUser(User.withUsername("admin").password(passwordEncoder.encode("pw")).authorities("ROLE_ADMIN", "ROLE_USER").build());
        userDetailsManager.createUser(User.withUsername("user").password(passwordEncoder.encode("pw")).authorities("ROLE_USER").build());

        // CachingUserDetailsService cachingUserDetailsService = new CachingUserDetailsService(userDetailsManager);
        // cachingUserDetailsService.setUserCache(userCache);
        //
        // UserDetailsService userDetailsService = cachingUserDetailsService;

        UserDetailsService userDetailsService = userDetailsManager;

        return userDetailsService;
    }

    /**
     * @param tokenStore {@link TokenStore}
     * @return {@link DefaultTokenServices}
     */
    @Bean
    @Primary
    public DefaultTokenServices tokenServices(final TokenStore tokenStore)
    {
        DefaultTokenServices defaultTokenServices = new DefaultTokenServices();
        defaultTokenServices.setTokenStore(tokenStore);
        defaultTokenServices.setSupportRefreshToken(true);

        return defaultTokenServices;
    }

    /**
     * @param accessTokenConverter {@link JwtAccessTokenConverter}
     * @return {@link TokenStore}
     */
    @Bean
    public TokenStore tokenStore(final JwtAccessTokenConverter accessTokenConverter)
    {
        return new JwtTokenStore(accessTokenConverter);
    }
}
