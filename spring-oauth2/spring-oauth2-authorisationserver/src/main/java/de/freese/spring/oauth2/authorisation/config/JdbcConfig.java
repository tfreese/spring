// Created: 30.10.2018
package de.freese.spring.oauth2.authorisation.config;

import javax.sql.DataSource;

import org.hsqldb.Database;
import org.hsqldb.server.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
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
class JdbcConfig
{
    /**
     * @return {@link AccessTokenConverter}
     */
    @Bean
    AccessTokenConverter accessTokenConverter()
    {
        return new DefaultAccessTokenConverter();
    }

    /**
     * @param dataSource {@link DataSource}
     *
     * @return {@link ApprovalStore}
     */
    @Bean
    @DependsOn("hsqldbServer")
    ApprovalStore approvalStore(final DataSource dataSource)
    {
        return new JdbcApprovalStore(dataSource);
    }

    /**
     * @param dataSource {@link DataSource}
     *
     * @return {@link AuthorizationCodeServices}
     */
    @Bean
    @DependsOn("hsqldbServer")
    AuthorizationCodeServices authorizationCodeServices(final DataSource dataSource)
    {
        return new JdbcAuthorizationCodeServices(dataSource);
    }

    /**
     * @param dbName String
     * @param dbPath String
     * @param port int
     *
     * @return {@link Server}
     */
    @Bean(initMethod = "start", destroyMethod = "shutdown")
    Server hsqldbServer(@Value("${hsqldb.db.name}") final String dbName, @Value("${hsqldb.db.path}") final String dbPath,
                        @Value("${hsqldb.server.port}") final int port)
    {
        Server server = new Server()
        {
            /**
             * @see org.hsqldb.server.Server#shutdown()
             */
            @Override
            public void shutdown()
            {
                // "SHUTDOWN COMPACT"
                super.shutdownWithCatalogs(Database.CLOSEMODE_COMPACT);
            }

        };
        server.setLogWriter(null);
        server.setErrWriter(null);
        // server.setLogWriter(new PrintWriter(System.out)); // can use custom writer
        // server.setErrWriter(new PrintWriter(System.err)); // can use custom writer
        server.setNoSystemExit(true);
        server.setSilent(true);
        server.setTrace(false);
        server.setPort(port);

        server.setDatabaseName(0, dbName);
        server.setDatabasePath(0, dbPath);

        return server;
    }

    /**
     * @param dataSource {@link DataSource}
     *
     * @return {@link ClientDetailsService}
     */
    @Bean
    @DependsOn("hsqldbServer")
    ClientDetailsService myClientDetailsService(final DataSource dataSource)
    {
        return new JdbcClientDetailsService(dataSource);
    }

    /**
     * @param dataSource {@link DataSource}
     * @param userCache {@link UserCache}
     *
     * @return {@link UserDetailsService}
     */
    @Bean
    @DependsOn("hsqldbServer")
    UserDetailsService myUserDetailsService(final DataSource dataSource, final UserCache userCache)
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
     *
     * @return {@link TokenStore}
     */
    @Bean
    @DependsOn("hsqldbServer")
    TokenStore tokenStore(final DataSource dataSource)
    {
        return new JdbcTokenStore(dataSource);
    }
}
