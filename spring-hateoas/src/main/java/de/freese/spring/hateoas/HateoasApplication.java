// Created: 04.05.2016
package de.freese.spring.hateoas;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.hateoas.config.EnableHypermediaSupport;

/**
 * <a href="https://spring.io/guides/gs/rest-hateoas">rest-hateoas</a><br>
 * <a href="https://github.com/spring-guides/gs-rest-hateoas">gs-rest-hateoas</a><br>
 * curl http://localhost:9000/greeter<br>
 *
 * @author Thomas Freese
 */
@SpringBootApplication
@EnableHypermediaSupport(type = EnableHypermediaSupport.HypermediaType.HAL)
public class HateoasApplication
{
    public static void main(final String[] args)
    {
        SpringApplication.run(HateoasApplication.class, args);
    }
}
