package de.spring.ai.mcp.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author Thomas Freese
 */
@SpringBootApplication
public final class McpServerApplication {
    static void main(final String[] args) {
        SpringApplication.run(McpServerApplication.class, args);
    }

    private McpServerApplication() {
        super();
    }
}
