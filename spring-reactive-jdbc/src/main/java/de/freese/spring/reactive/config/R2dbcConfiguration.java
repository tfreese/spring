// Created: 12.10.2021
package de.freese.spring.reactive.config;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import io.r2dbc.spi.ConnectionFactories;
import io.r2dbc.spi.ConnectionFactory;
import io.r2dbc.spi.ConnectionFactoryOptions;

/**
 * @author Thomas Freese
 */
@Configuration
@Profile("r2dbc")
@EnableAutoConfiguration(exclude =
{
        DataSourceAutoConfiguration.class
})
public class R2dbcConfiguration // extends AbstractR2dbcConfiguration
{
    /**
     * @return {@link ConnectionFactory}
     */
    public ConnectionFactory connectionFactory()
    {
        // :create=true;shutdown=true
        // return ConnectionFactories.get("r2dbc:h2:mem:testR2dbc");

        // return new H2ConnectionFactory(H2ConnectionConfiguration.builder()
        // .inMemory(testR2dbc)
        // .option(H2ConnectionOption.DB_CLOSE_DELAY, "-1")
        // .build());

        // @formatter:off
        ConnectionFactoryOptions options = ConnectionFactoryOptions.builder()
                .option(ConnectionFactoryOptions.DRIVER, "h2")
                .option(ConnectionFactoryOptions.PROTOCOL, "mem")
                //.option(ConnectionFactoryOptions.HOST, "…")
                //.option(ConnectionFactoryOptions.port, 8123)
                .option(ConnectionFactoryOptions.USER, "SA")
                .option(ConnectionFactoryOptions.PASSWORD, "")
                .option(ConnectionFactoryOptions.DATABASE, "testR2dbc")
                .build()
                ;
        // @formatter:on

        return ConnectionFactories.get(options);
    }

    // /**
    // * @param connectionFactory {@link ConnectionFactory}
    // *
    // * @return {@link ConnectionFactoryInitializer}
    // */
    // @Bean
    // public ConnectionFactoryInitializer initializer(final ConnectionFactory connectionFactory)
    // {
    // ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
    // populator.addScript(new ClassPathResource("sql/schema-h2.sql"));
    // populator.addScript(new ClassPathResource("sql/data.sql"));
    //
    // ConnectionFactoryInitializer initializer = new ConnectionFactoryInitializer();
    // initializer.setConnectionFactory(connectionFactory);
    // initializer.setDatabasePopulator(populator);
    //
    // return initializer;
    // }
}
