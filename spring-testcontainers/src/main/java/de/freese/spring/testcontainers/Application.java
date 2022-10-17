package de.freese.spring.testcontainers;

import java.sql.SQLException;

import javax.sql.DataSource;

import de.freese.jdbc.dialect.JdbcDialect;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

/**
 * @author Thomas Freese
 */
@SpringBootApplication
public class Application
{
    public static void main(String[] args)
    {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    JdbcDialect jdbcDialect(DataSource dataSource) throws SQLException
    {
        return JdbcDialect.from(dataSource);
    }
}