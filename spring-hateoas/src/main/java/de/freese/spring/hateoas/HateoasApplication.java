// Created: 04.05.2016
package de.freese.spring.hateoas;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.hateoas.config.EnableHypermediaSupport;

/**
 * https://spring.io/guides/gs/rest-hateoas<br>
 * https://github.com/spring-guides/gs-rest-hateoas<br>
 * curl http://localhost:9000/greeter<br>
 *
 * @author Thomas Freese
 */
@SpringBootApplication
@EnableHypermediaSupport(type = EnableHypermediaSupport.HypermediaType.HAL)
public class HateoasApplication
{
    /**
     * @param args String[]
     */
    public static void main(final String[] args)
    {
        SpringApplication.run(HateoasApplication.class, args);
    }
}
