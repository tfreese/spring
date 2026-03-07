package de.spring.ai.chatbot.mcp.client;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author Thomas Freese
 */
@SpringBootApplication
public final class McpClientApplication {
    static void main(final String[] args) {
        SpringApplication.run(McpClientApplication.class, args);
    }

    private McpClientApplication() {
        super();
    }
}
