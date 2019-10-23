/**
 * Created: 30.10.2018
 */

package de.freese.spring.oauth2.authorisation.config;

import javax.sql.DataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserCache;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.config.annotation.builders.InMemoryClientDetailsServiceBuilder;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.approval.ApprovalStore;
import org.springframework.security.oauth2.provider.approval.InMemoryApprovalStore;
import org.springframework.security.oauth2.provider.code.AuthorizationCodeServices;
import org.springframework.security.oauth2.provider.code.InMemoryAuthorizationCodeServices;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.InMemoryTokenStore;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;

/**
 * @author Thomas Freese
 */
@Configuration
@Profile("memory")
public class MemoryConfig
{
    /**
     * Erstellt ein neues {@link MemoryConfig} Object.
     */
    public MemoryConfig()
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
                    .redirectUris("http://localhost:8082/ui/login", "http://localhost:8083/ui2/login", "http://localhost:8082/login")
                    .autoApprove(false)
                    .accessTokenValiditySeconds(120) // 2 Minuten
                    .refreshTokenValiditySeconds(3600) // 1 Stunde
                .and()
                .withClient("my-client-id-write")
                    .resourceIds("my-oauth-app")
                    .secret("{noop}my-secret")
                    .authorizedGrantTypes("authorization_code", "client_credentials", "password", "refresh_token", "implicit")
                    .authorities("USER", "ADMIN")
                    .scopes("user_info", "read", "write")
                    .redirectUris("http://localhost:8082/ui/login", "http://localhost:8083/ui2/login", "http://localhost:8082/login")
                    .autoApprove(false)
                    .accessTokenValiditySeconds(120) // 2 Minuten
                    .refreshTokenValiditySeconds(3600) // 1 Stunde
                .and()
                .build()
        ;
        // @formatter:on
    }

    /**
     * @param dataSource {@link DataSource}
     * @param userCache {@link UserCache}
     * @return {@link UserDetailsService}
     */
    @Bean
    public UserDetailsService myUserDetailsService(final DataSource dataSource, final UserCache userCache)
    {
        // "{bcrypt}" + passwordEncoder.encode("pw")
        // PasswordEncoder passwordEncoder = passwordEncoder();

        InMemoryUserDetailsManager userDetailsManager = new InMemoryUserDetailsManager();
        userDetailsManager.createUser(User.withUsername("admin").password("{NOOP}pw").roles("ADMIN", "USER").build());
        userDetailsManager.createUser(User.withUsername("user").password("{NOOP}pw").roles("USER").build());

        // CachingUserDetailsService cachingUserDetailsService = new CachingUserDetailsService(userDetailsManager);
        // cachingUserDetailsService.setUserCache(userCache);
        //
        // UserDetailsService userDetailsService = cachingUserDetailsService;

        UserDetailsService userDetailsService = userDetailsManager;

        return userDetailsService;
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
