// Created: 10.10.2017
package de.freese.spring.boot.admin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import de.codecentric.boot.admin.server.config.EnableAdminServer;

/**
 * @author Thomas Freese
 */
@SpringBootApplication
@EnableAdminServer
public class SpringBootAdminApplication
{
    /**
     * @param args String[]
     */
    public static void main(final String[] args)
    {
        SpringApplication.run(SpringBootAdminApplication.class, args);
    }
}
