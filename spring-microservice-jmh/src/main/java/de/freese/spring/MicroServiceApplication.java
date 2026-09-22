package de.freese.spring;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * curl http://localhost:8081
 *
 * @author Thomas Freese
 * @since 14.02.2017
 */
@SpringBootApplication
@RestController
public class MicroServiceApplication {
    static void main(final String[] args) {
        SpringApplication.run(MicroServiceApplication.class, args);
    }

    @GetMapping(path = "/", produces = MediaType.APPLICATION_JSON_VALUE)
    public String greeting() {
        return "Hello, World";
    }
}
