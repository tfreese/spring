package de.freese.kubernetes.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author Thomas Freese
 */
@SpringBootApplication
@SuppressWarnings("checkstyle:HideUtilityClassConstructor")
public class MyApplication {
    static void main(final String[] args) {
        SpringApplication.run(MyApplication.class, args);
    }
}
