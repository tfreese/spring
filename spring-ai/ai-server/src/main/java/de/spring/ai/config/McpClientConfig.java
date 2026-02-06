package de.spring.ai.config;

import java.util.List;

import io.modelcontextprotocol.client.McpSyncClient;
import org.springframework.ai.mcp.McpToolFilter;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Thomas Freese
 */
@Configuration
public class McpClientConfig {
    @Bean
    ToolCallbackProvider myToolCallbackProvider(final List<McpSyncClient> mcpSyncClients, final McpToolFilter mcpToolFilter) {
        return SyncMcpToolCallbackProvider.builder()
                .mcpClients(mcpSyncClients)
                .toolFilter(mcpToolFilter)
                .build();
    }
}
