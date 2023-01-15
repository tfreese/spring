// Created: 10.10.2017
package de.freese.spring.boot.cloud.admin;

import de.codecentric.boot.admin.server.config.EnableAdminServer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author Thomas Freese
 */
@SpringBootApplication
@EnableAdminServer
public final class SpringBootAdminApplication
{
    public static void main(final String[] args)
    {
        SpringApplication.run(SpringBootAdminApplication.class, args);
    }

    private SpringBootAdminApplication()
    {
        super();
    }
}
