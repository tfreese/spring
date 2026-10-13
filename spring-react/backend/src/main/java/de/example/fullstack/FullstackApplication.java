package de.example.fullstack;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public final class FullstackApplication {
    static void main(final String[] args) {
        SpringApplication.run(FullstackApplication.class, args);
    }

    private FullstackApplication() {
        super();
    }
}
