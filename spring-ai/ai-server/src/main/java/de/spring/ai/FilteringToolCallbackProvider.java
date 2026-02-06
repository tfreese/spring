package de.spring.ai;

import java.util.Arrays;
import java.util.Objects;
import java.util.Set;

import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;

/**
 * Alternative for {@link org.springframework.ai.mcp.McpToolFilter}.
 *
 * @author Thomas Freese
 */
public final class FilteringToolCallbackProvider implements ToolCallbackProvider {

    private final Set<String> allowedToolNames;
    private final ToolCallbackProvider delegate;

    public FilteringToolCallbackProvider(final ToolCallbackProvider delegate, final Set<String> allowedToolNames) {
        super();

        this.delegate = Objects.requireNonNull(delegate, "delegate required");
        this.allowedToolNames = Objects.requireNonNull(allowedToolNames, "allowedToolNames required");
    }

    @Override
    public ToolCallback[] getToolCallbacks() {
        return Arrays.stream(delegate.getToolCallbacks())
                .filter(tc -> allowedToolNames.contains(tc.getToolDefinition().name()))
                .toArray(ToolCallback[]::new);
    }
}
