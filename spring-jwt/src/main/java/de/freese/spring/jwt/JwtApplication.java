package de.freese.spring.jwt;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import de.freese.spring.jwt.config.RsaKeyProperties;

@EnableConfigurationProperties(RsaKeyProperties.class)
@SpringBootApplication
public class JwtApplication {
    public static void main(final String[] args) {
        SpringApplication.run(JwtApplication.class, args);
    }
}
