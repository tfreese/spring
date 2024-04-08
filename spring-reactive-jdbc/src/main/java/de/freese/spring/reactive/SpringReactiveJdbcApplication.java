package de.freese.spring.reactive;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * curl localhost:8080/departments<br>
 * curl localhost:8080/employees<br>
 * <a href="https://github.com/netifi/webflux-rxjava2-jdbc-example">webflux-rxjava2-jdbc-example</a><br>
 * <a href="https://github.com/hantsy/spring-r2dbc-sample/tree/master/database-client">database-client</a><br>
 * <a href="https://spring.io/blog/2019/05/16/reactive-transactions-with-spring">reactive-transactions-with-spring</a><br>
 *
 * @author Thomas Freese
 */
@SpringBootApplication// (exclude = R2dbcAutoConfiguration.class)
@EnableTransactionManagement
@SuppressWarnings("checkstyle:HideUtilityClassConstructor")
public class SpringReactiveJdbcApplication {
    public static void main(final String[] args) {
        SpringApplication.run(SpringReactiveJdbcApplication.class, args);
    }
}
