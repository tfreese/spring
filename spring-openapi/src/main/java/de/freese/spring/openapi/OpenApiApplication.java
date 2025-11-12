package de.freese.spring.openapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * <a href="http://localhost:8080/swagger-ui/index.html">http://localhost:8080/swagger-ui/index.html</a>
 * <a href="http://localhost:8080/swagger-ui.html">http://localhost:8080/swagger-ui.html</a>
 * <a href="http://localhost:8080/api-docs">http://localhost:8080/api-docs</a>
 * <a href="http://localhost:8080/api-docs.yaml">http://localhost:8080/api-docs.yaml</a>
 *
 * @author Thomas Freese
 */
@SpringBootApplication
// @SuppressWarnings("checkstyle:HideUtilityClassConstructor")
public final class OpenApiApplication {
    static void main(final String[] args) {
        SpringApplication.run(OpenApiApplication.class, args);
    }

    private OpenApiApplication() {
        super();
    }
}
