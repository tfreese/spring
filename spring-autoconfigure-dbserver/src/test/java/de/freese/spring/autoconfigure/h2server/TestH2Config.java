// Created: 19.10.2019
package de.freese.spring.autoconfigure.h2server;

import java.sql.SQLException;

import javax.sql.DataSource;

import org.h2.tools.Server;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * @author Thomas Freese
 */
@SpringBootApplication // Mit Configuration wird die application.yml nicht eingelesen.
public class TestH2Config
{
    /**
     * @return {@link javax.sql.DataSource}
     */
    @Bean
    @ConfigurationProperties(prefix = "datasource.h2.file")
    public DataSource dataSourceH2File()
    {
        return DataSourceBuilder.create().build();
    }

    /**
     * @return {@link javax.sql.DataSource}
     */
    @Bean
    @ConfigurationProperties(prefix = "datasource.h2.memory")
    public DataSource dataSourceH2Memory()
    {
        return DataSourceBuilder.create().build();
    }

    /**
     * @param port int
     * @param path String
     *
     * @return {@link Server}
     *
     * @throws SQLException Falls was schief geht.
     */
    @Bean(initMethod = "start", destroyMethod = "stop")
    public Server serverH2(@Value("${h2.port}") final int port, @Value("${h2.path}") final String path) throws SQLException
    {
        // while (!server.isRunning(true))
        // {
        // TimeUnit.MILLISECONDS.sleep(100);
        // }

        // , "-trace"
        return Server.createTcpServer("-tcpPort", Integer.toString(port), "-trace", "-tcpDaemon", "-ifNotExists", "-baseDir", path);
    }

    /**
     * @param dataSource {@link DataSource}
     *
     * @return {@link PlatformTransactionManager}
     */
    @Bean
    public PlatformTransactionManager transactionManagerH2File(@Qualifier("dataSourceH2File") final DataSource dataSource)
    {
        return new DataSourceTransactionManager(dataSource);
    }

    /**
     * @param dataSource {@link DataSource}
     *
     * @return {@link PlatformTransactionManager}
     */
    @Bean
    public PlatformTransactionManager transactionManagerH2Memory(@Qualifier("dataSourceH2Memory") final DataSource dataSource)
    {
        return new DataSourceTransactionManager(dataSource);
    }
}
