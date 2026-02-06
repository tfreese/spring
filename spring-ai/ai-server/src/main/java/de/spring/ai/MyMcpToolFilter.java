package de.spring.ai;

import io.modelcontextprotocol.spec.McpSchema;
import org.springframework.ai.mcp.McpConnectionInfo;
import org.springframework.ai.mcp.McpToolFilter;
import org.springframework.stereotype.Component;

/**
 * Or use custom ToolCallbackProvider like {@link FilteringToolCallbackProvider}.
 *
 * @author Thomas Freese
 */
@Component
public class MyMcpToolFilter implements McpToolFilter {
    @Override
    public boolean test(final McpConnectionInfo mcpConnectionInfo, final McpSchema.Tool tool) {
        // Filter logic based on connection information and tool properties.
        // Return true to include the tool, false to exclude it.

        // Example: Exclude tools from a specific client.
        if ("restricted-client".equals(mcpConnectionInfo.clientInfo().name())) {
            return false;
        }

        // if ("addProduct".equals(tool.name())) {
        //     return false;
        // }

        // Example: Filter based on tool description or other properties.
        return tool.description() == null || !tool.description().contains("experimental");
    }
}
