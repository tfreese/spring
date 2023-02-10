// Created: 19.10.2019
package de.freese.spring.autoconfigure.hsqldbserver;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Qualifier;
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
public class TestHsqldbConfig {
    @Bean
    @ConfigurationProperties(prefix = "datasource.hsqldb.file")
    public DataSource dataSourceHsqldbFile() {
        return DataSourceBuilder.create().build();
    }

    @Bean
    @ConfigurationProperties(prefix = "datasource.hsqldb.memory")
    public DataSource dataSourceHsqldbMemory() {
        return DataSourceBuilder.create().build();
    }

    @Bean
    public PlatformTransactionManager transactionManagerHsqldbFile(@Qualifier("dataSourceHsqldbFile") final DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

    @Bean
    public PlatformTransactionManager transactionManagerHsqldbMemory(@Qualifier("dataSourceHsqldbMemory") final DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }
}
