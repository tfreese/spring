// Created: 12.10.2021
package de.freese.spring.reactive.config;

import java.util.UUID;

import javax.sql.DataSource;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.r2dbc.R2dbcAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;

/**
 * @author Thomas Freese
 */
@Configuration
@Profile("jdbc")
@EnableAutoConfiguration(exclude = {R2dbcAutoConfiguration.class})
public class JdbcConfiguration {
    @Bean
    DataSource dataSource() {
        final String id = UUID.randomUUID().toString();

        return new EmbeddedDatabaseBuilder().setType(EmbeddedDatabaseType.H2).setName(id).build();

        // return DataSourceBuilder.create()
        //         .driverClassName("org.h2.Driver")
        //         .url("jdbc:h2:mem:" + id) // :create=true;shutdown=true
        //         .username("SA")
        //         .password("")
        //         .build()
        //         ;
    }

    // /**
    // * Wird automatisch erzeugt.
    // */
    // @Bean
    // TransactionManager transactionManager(final DataSource dataSource) {
    // return new DataSourceTransactionManager(dataSource);
    // }

    // @Bean
    // DataSourceInitializer initializer(final DataSource dataSource) {
    // final ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
    // populator.addScript(new ClassPathResource("sql/schema.sql"));
    // populator.addScript(new ClassPathResource("sql/data.sql"));
    // // populator.execute(this.dataSource);
    //
    // final DataSourceInitializer dataSourceInitializer = new DataSourceInitializer();
    // dataSourceInitializer.setDataSource(dataSource);
    // dataSourceInitializer.setDatabasePopulator(populator);
    //
    // return dataSourceInitializer;
    // }
}
