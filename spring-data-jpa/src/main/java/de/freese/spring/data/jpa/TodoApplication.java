// Created: 16.08.23
package de.freese.spring.data.jpa;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * <a href="https://codaholic.com/spring-boot-guide/">spring-boot-guide</a><br>
 * <a href="https://github.com/codaholichq/todo">codaholichq github</a><br>
 * <br>
 * <pre>
 * curl -X POST localhost:8080/api/todo \
 *    -H 'Content-Type: application/json' \
 *    -d '{"name":"Buy Something","startTime":"2023-08-16T17:00","endTime":"2023-08-16T18:00"}'
 *
 * curl -X GET localhost:8080/api/todo
 * </pre>
 *
 * @author Thomas Freese
 */
@SpringBootApplication
@EnableTransactionManagement
public class TodoApplication {
    public static void main(final String[] args) {
        SpringApplication.run(TodoApplication.class, args);
    }
}
