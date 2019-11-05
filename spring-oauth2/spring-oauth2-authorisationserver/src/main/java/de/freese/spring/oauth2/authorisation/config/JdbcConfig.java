/**
 * Created: 30.10.2018
 */

package de.freese.spring.oauth2.authorisation.config;

import javax.sql.DataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.CachingUserDetailsService;
import org.springframework.security.core.userdetails.UserCache;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.jdbc.JdbcDaoImpl;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.approval.ApprovalStore;
import org.springframework.security.oauth2.provider.approval.JdbcApprovalStore;
import org.springframework.security.oauth2.provider.client.JdbcClientDetailsService;
import org.springframework.security.oauth2.provider.code.AuthorizationCodeServices;
import org.springframework.security.oauth2.provider.code.JdbcAuthorizationCodeServices;
import org.springframework.security.oauth2.provider.token.AccessTokenConverter;
import org.springframework.security.oauth2.provider.token.DefaultAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JdbcTokenStore;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * @author Thomas Freese
 */
@Configuration
@EnableTransactionManagement
@Profile("jdbc")
public class JdbcConfig
{
    /**
     * Erstellt ein neues {@link JdbcConfig} Object.
     */
    public JdbcConfig()
    {
        super();
    }

    /**
     * @return {@link AccessTokenConverter}
     */
    @Bean
    public AccessTokenConverter accessTokenConverter()
    {
        return new DefaultAccessTokenConverter();
    }

    /**
     * @param dataSource {@link DataSource}
     * @return {@link ApprovalStore}
     */
    @Bean
    public ApprovalStore approvalStore(final DataSource dataSource)
    {
        return new JdbcApprovalStore(dataSource);
    }

    /**
     * @param dataSource {@link DataSource}
     * @return {@link AuthorizationCodeServices}
     */
    @Bean
    public AuthorizationCodeServices authorizationCodeServices(final DataSource dataSource)
    {
        return new JdbcAuthorizationCodeServices(dataSource);
    }

    /**
     * @param dataSource {@link DataSource}
     * @return {@link ClientDetailsService}
     */
    @Bean
    public ClientDetailsService myClientDetailsService(final DataSource dataSource)
    {
        return new JdbcClientDetailsService(dataSource);
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

        JdbcDaoImpl jdbcDao = new JdbcDaoImpl();
        jdbcDao.setDataSource(dataSource);
        jdbcDao.setUsersByUsernameQuery("select username, password, enabled from USER where username = ?");
        jdbcDao.setAuthoritiesByUsernameQuery("select username, role from AUTHORITY where username = ?");

        CachingUserDetailsService cachingUserDetailsService = new CachingUserDetailsService(jdbcDao);
        cachingUserDetailsService.setUserCache(userCache);

        UserDetailsService userDetailsService = cachingUserDetailsService;

        return userDetailsService;
    }

    /**
     * @param dataSource {@link DataSource}
     * @return {@link TokenStore}
     */
    @Bean
    public TokenStore tokenStore(final DataSource dataSource)
    {
        return new JdbcTokenStore(dataSource);
    }
}
