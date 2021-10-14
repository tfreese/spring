package de.freese.spring.reactive;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * curl localhost:8080/departments<br>
 * curl localhost:8080/employees<br>
 * {@link "https://github.com/netifi/webflux-rxjava2-jdbc-example"}<br>
 * {@link "https://github.com/hantsy/spring-r2dbc-sample/tree/master/database-client"}<br>
 * {@link "https://spring.io/blog/2019/05/16/reactive-transactions-with-spring"}<br>
 *
 * @author Thomas Freese
 */
@SpringBootApplication// (exclude = R2dbcAutoConfiguration.class)
@EnableTransactionManagement
public class SpringReactiveJdbcApplication
{
    /**
     * @param args String[]
     */
    public static void main(final String[] args)
    {
        SpringApplication.run(SpringReactiveJdbcApplication.class, args);
    }
}
