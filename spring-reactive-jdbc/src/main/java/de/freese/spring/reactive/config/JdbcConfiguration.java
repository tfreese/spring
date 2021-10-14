// Created: 12.10.2021
package de.freese.spring.reactive.config;

import javax.sql.DataSource;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.r2dbc.R2dbcAutoConfiguration;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * @author Thomas Freese
 */
@Configuration
@Profile("jdbc")
@EnableAutoConfiguration(exclude =
{
        R2dbcAutoConfiguration.class
})
public class JdbcConfiguration
{
    /**
     * @return {@link DataSource}
     */
    @Bean
    DataSource dataSource()
    {
        // return new EmbeddedDatabaseBuilder().setType(EmbeddedDatabaseType.H2).setName("testJdbc").build();

        // @formatter:off
        return DataSourceBuilder.create()
                .driverClassName("org.h2.Driver")
                .url("jdbc:h2:mem:testJdbc") // :create=true;shutdown=true
                .username("SA")
                .password("")
                .build()
                ;
        // @formatter:on
    }

    // /**
    // * Wird automatisch erzeugt.
    // *
    // * @param dataSource {@link DataSource}
    // *
    // * @return {@link TransactionManager}
    // */
    // @Bean
    // TransactionManager transactionManager(final DataSource dataSource)
    // {
    // return new DataSourceTransactionManager(dataSource);
    // }

    // /**
    // * @param dataSource {@link DataSource}
    // *
    // * @return {@link DataSourceInitializer}
    // */
    // @Bean
    // DataSourceInitializer initializer(final DataSource dataSource)
    // {
    // ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
    // populator.addScript(new ClassPathResource("sql/schema-h2.sql"));
    // populator.addScript(new ClassPathResource("sql/data.sql"));
    // // populator.execute(this.dataSource);
    //
    // DataSourceInitializer dataSourceInitializer = new DataSourceInitializer();
    // dataSourceInitializer.setDataSource(dataSource);
    // dataSourceInitializer.setDatabasePopulator(populator);
    //
    // return dataSourceInitializer;
    // }
}
