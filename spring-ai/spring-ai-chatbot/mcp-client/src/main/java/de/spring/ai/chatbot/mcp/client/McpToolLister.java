package de.spring.ai.chatbot.mcp.client;

import java.util.List;
import java.util.Objects;

import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.spec.McpSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.definition.ToolDefinition;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * @author Thomas Freese
 */
@Component
public class McpToolLister implements CommandLineRunner {
    private static final Logger LOGGER = LoggerFactory.getLogger(McpToolLister.class);

    private final List<McpSyncClient> mcpClients;

    public McpToolLister(final List<McpSyncClient> mcpClients) {
        super();

        this.mcpClients = Objects.requireNonNull(mcpClients, "mcpClients required");
    }

    @Override
    public void run(final String... args) throws Exception {
        LOGGER.info("Discovering MCP Tools");

        for (McpSyncClient client : mcpClients) {
            final McpSchema.Implementation implementation = client.getClientInfo();

            if (implementation == null) {
                LOGGER.warn("Implementation is null for: {}", client);
                continue;
            }

            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("Connected to MCP Client: {}", implementation.name());
            }

            // Use the provider to get ToolCallbacks from the client.
            final SyncMcpToolCallbackProvider provider = SyncMcpToolCallbackProvider.builder().mcpClients(client).build();
            final List<ToolCallback> toolCallbacks = List.of(provider.getToolCallbacks());

            if (toolCallbacks.isEmpty()) {
                LOGGER.warn("No tools found on this MCP client.");
            }
            else {
                for (ToolCallback toolCallback : toolCallbacks) {
                    final ToolDefinition toolDefinition = toolCallback.getToolDefinition();

                    if (LOGGER.isInfoEnabled()) {
                        LOGGER.info("Tool Name: {}", toolDefinition.name());
                        LOGGER.info("   Description: {}", toolDefinition.description());
                        LOGGER.info("   Input Schema: {}", toolDefinition.inputSchema());
                    }
                }
            }
        }
    }
}
