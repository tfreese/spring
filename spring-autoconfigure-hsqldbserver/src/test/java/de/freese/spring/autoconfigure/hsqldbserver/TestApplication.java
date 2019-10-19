/**
 * Created: 19.10.2019
 */

package de.freese.spring.autoconfigure.hsqldbserver;

import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * @author Thomas Freese
 */
@SpringBootApplication
public class TestApplication
{
    /**
     * @param args String[]
     */
    public static void main(final String[] args)
    {
        SpringApplication.run(TestApplication.class, args);
    }

    /**
     * @return {@link javax.sql.DataSource}
     */
    @Bean
    @ConfigurationProperties(prefix = "datasource.file")
    public DataSource dataSourceFile()
    {
        return DataSourceBuilder.create().build();
    }

    /**
     * @return {@link javax.sql.DataSource}
     */
    @Bean
    @ConfigurationProperties(prefix = "datasource.memory")
    public DataSource dataSourceMemory()
    {
        return DataSourceBuilder.create().build();
    }

    /**
     * @param dataSource {@link DataSource}
     * @return {@link PlatformTransactionManager}
     */
    @Bean
    public PlatformTransactionManager transactionManagerFile(@Qualifier("dataSourceFile") final DataSource dataSource)
    {
        return new DataSourceTransactionManager(dataSource);
    }

    /**
     * @param dataSource {@link DataSource}
     * @return {@link PlatformTransactionManager}
     */
    @Bean
    public PlatformTransactionManager transactionManagerMem(@Qualifier("dataSourceMemory") final DataSource dataSource)
    {
        return new DataSourceTransactionManager(dataSource);
    }
}
