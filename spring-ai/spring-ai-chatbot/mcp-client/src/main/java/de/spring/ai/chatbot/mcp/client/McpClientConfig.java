package de.spring.ai.chatbot.mcp.client;

import java.util.List;

import io.modelcontextprotocol.client.McpSyncClient;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Thomas Freese
 */
@Configuration
public class McpClientConfig {
    @Bean
    public SyncMcpToolCallbackProvider myToolCallbackProvider(final List<McpSyncClient> mcpSyncClients) {
        return SyncMcpToolCallbackProvider.builder()
                .mcpClients(mcpSyncClients)
                .build();
    }
}
