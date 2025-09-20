// Created: 14.02.2017
package de.freese.spring.cloud.eureka;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

/**
 * @author Thomas Freese
 */
@SpringBootApplication
@EnableEurekaServer
@SuppressWarnings("checkstyle:HideUtilityClassConstructor")
public class EurekaServerApplication {
    static void main(final String[] args) {
        SpringApplication.run(EurekaServerApplication.class, args);
    }
}
