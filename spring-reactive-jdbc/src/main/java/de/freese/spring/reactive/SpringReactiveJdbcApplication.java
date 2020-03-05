package de.freese.spring.reactive;

import javax.annotation.Resource;
import javax.sql.DataSource;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * {@link "https://github.com/netifi/webflux-rxjava2-jdbc-example"}
 *
 * @author Thomas Freese
 */
@SpringBootApplication
@EnableTransactionManagement
public class SpringReactiveJdbcApplication
{
    /**
     * @author Thomas Freese
     */
    @Configuration
    @Profile("!test")
    class DBConfig implements CommandLineRunner
    {
        /**
         *
         */
        @Resource
        private DataSource dataSource = null;

        /**
         * @see org.springframework.boot.CommandLineRunner#run(java.lang.String[])
         */
        @Override
        public void run(final String...args) throws Exception
        {
            ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
            populator.addScript(new ClassPathResource("sql/schema-h2.sql"));
            populator.addScript(new ClassPathResource("sql/data.sql"));
            populator.execute(this.dataSource);
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
}
