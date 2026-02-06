package de.spring.ai.mcp.server;

import java.util.List;

import de.spring.ai.mcp.server.product.ProductService;
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
    List<ToolCallback> myTools(final ProductService productService) {
        // return MethodToolCallbackProvider
        //         .builder()
        //         .toolObjects(productService)
        //         .build();

        return List.of(ToolCallbacks.from(productService));
    }
}
