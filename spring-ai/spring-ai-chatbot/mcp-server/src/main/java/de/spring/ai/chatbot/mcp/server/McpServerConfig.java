package de.spring.ai.chatbot.mcp.server;

import java.util.List;

import de.spring.ai.chatbot.mcp.server.inventory.InventoryService;
import de.spring.ai.chatbot.mcp.server.product.ProductService;
import org.springframework.ai.support.ToolCallbacks;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Thomas Freese
 */
@Configuration
public class McpServerConfig {
    @Bean
    public List<ToolCallback> myTools(final ProductService productService, final InventoryService inventoryService) {
        // return MethodToolCallbackProvider
        //         .builder()
        //         .toolObjects(productService, inventoryService)
        //         .build();

        return List.of(ToolCallbacks.from(productService, inventoryService, new ChatTools()));
    }
}
