package de.freese.spring.reactive;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * {@link "https://github.com/netifi/webflux-rxjava2-jdbc-example"}
 *
 * @author Thomas Freese
 */
@SpringBootApplication
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
