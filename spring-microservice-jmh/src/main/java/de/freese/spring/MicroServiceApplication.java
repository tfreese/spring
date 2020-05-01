// Created: 14.02.2017
package de.freese.spring;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * curl http://localhost:8081
 *
 * @author Thomas Freese
 */
@SpringBootApplication
@RestController
public class MicroServiceApplication
{
    /**
     * @param args String[]
     */
    @SuppressWarnings("resource")
    public static void main(final String[] args)
    {
        SpringApplication.run(MicroServiceApplication.class, args);
    }

    /**
     * Erzeugt eine neue Instanz von {@link MicroServiceApplication}
     */
    public MicroServiceApplication()
    {
        super();
    }

    /**
     * @return String
     */
    @RequestMapping(path = "/", produces = MediaType.APPLICATION_JSON_VALUE)
    public String greeting()
    {
        return "Hello, World";
    }

    // /**
    // * @param builder {@link RestTemplateBuilder}
    // * @param serverPort int
    // * @return {@link RestTemplate}
    // */
    // @Bean
    // RestTemplate restTemplate(final RestTemplateBuilder builder, @Value("${local.server.port}") final int serverPort)
    // {
    // return builder.rootUri("http://localhost:" + serverPort).setConnectTimeout(Duration.ofMillis(3000)).setReadTimeout(Duration.ofMillis(3000)).build();
    // }
}
