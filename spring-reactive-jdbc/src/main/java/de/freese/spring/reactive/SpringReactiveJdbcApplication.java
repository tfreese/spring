package de.freese.spring.reactive;

import javax.sql.DataSource;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.r2dbc.config.AbstractR2dbcConfiguration;
import org.springframework.jdbc.datasource.init.DataSourceInitializer;
import org.springframework.r2dbc.connection.init.ConnectionFactoryInitializer;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import io.r2dbc.spi.ConnectionFactories;
import io.r2dbc.spi.ConnectionFactory;
import io.r2dbc.spi.ConnectionFactoryOptions;

/**
 * curl localhost:8080/departments<br>
 * curl localhost:8080/employees<br>
 * {@link "https://github.com/netifi/webflux-rxjava2-jdbc-example"}
 *
 * @author Thomas Freese
 */
@SpringBootApplication// (exclude = R2dbcAutoConfiguration.class)
@EnableTransactionManagement
public class SpringReactiveJdbcApplication
{
    /**
     * @author Thomas Freese
     */
    @Configuration
    @Profile("jdbc")
    public static class JdbcConfiguration
    {
        /**
         * @return {@link DataSource}
         */
        @Bean
        public DataSource dataSource()
        {
            // @formatter:off
            DataSource dataSource = DataSourceBuilder.create()
                    .driverClassName("org.h2.Driver")
                    .url("jdbc:h2:mem:testJdbc:create=true;shutdown=true")
                    .username("SA")
                    .password("")
                    .build()
                    ;
            // @formatter:on

            return dataSource;
        }

        /**
         * @param dataSource {@link DataSource}
         * @return {@link DataSourceInitializer}
         */
        @Bean
        public DataSourceInitializer initializer(final DataSource dataSource)
        {
            org.springframework.jdbc.datasource.init.ResourceDatabasePopulator populator =
                    new org.springframework.jdbc.datasource.init.ResourceDatabasePopulator();
            populator.addScript(new ClassPathResource("sql/schema-h2.sql"));
            populator.addScript(new ClassPathResource("sql/data.sql"));
            // populator.execute(this.dataSource);

            DataSourceInitializer dataSourceInitializer = new DataSourceInitializer();
            dataSourceInitializer.setDataSource(dataSource);
            dataSourceInitializer.setDatabasePopulator(populator);

            return dataSourceInitializer;
        }
    }

    /**
     * @author Thomas Freese
     */
    @Configuration
    @Profile("r2dbc")
    public static class R2dbcConfiguration extends AbstractR2dbcConfiguration
    {
        /**
         * @see org.springframework.data.r2dbc.config.AbstractR2dbcConfiguration#connectionFactory()
         */
        @Override
        public ConnectionFactory connectionFactory()
        {
            // return H2ConnectionFactory.inMemory("testR2dbc").create();
            // return ConnectionFactories.get("r2dbc:h2:mem:testR2dbc:create=true;shutdown=true");

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

        /**
         * @param connectionFactory {@link ConnectionFactory}
         * @return ConnectionFactoryInitializer
         */
        @Bean
        public ConnectionFactoryInitializer initializer(final ConnectionFactory connectionFactory)
        {
            org.springframework.r2dbc.connection.init.ResourceDatabasePopulator populator =
                    new org.springframework.r2dbc.connection.init.ResourceDatabasePopulator();
            populator.addScript(new ClassPathResource("sql/schema-h2.sql"));
            populator.addScript(new ClassPathResource("sql/data.sql"));

            ConnectionFactoryInitializer initializer = new ConnectionFactoryInitializer();
            initializer.setConnectionFactory(connectionFactory);
            initializer.setDatabasePopulator(populator);

            return initializer;
        }
    }

    /**
     * @param args String[]
     */
    @SuppressWarnings("resource")
    public static void main(final String[] args)
    {
        SpringApplication.run(SpringReactiveJdbcApplication.class, args);
    }

    // /**
    // * @return {@link DataSource}
    // */
    // @Bean
    // public DataSource dataSource()
    // {
    // // return DataSourceBuilder.create().build();
    // return new EmbeddedDatabaseBuilder().setType(EmbeddedDatabaseType.H2).setName("" + System.currentTimeMillis()).build();
    // }
}
