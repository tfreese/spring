package de.spring.ai.chatbot.mcp.client.chat;

import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * <a href="http://localhost:8082/mcp-chat?prompt=add apple juice to the products with price 1.99">add apple juice</a>
 * <a href="http://localhost:8082/mcp-chat?prompt=get the product id of apple juice">get product id of apple juice</a>
 * <a href="http://localhost:8082/mcp-chat?prompt=add product with id 1 with the availability of 15">add product</a>
 * <a href="http://localhost:8082/mcp-chat?prompt=what is the inventory id of the product with id 1">inventory id</a>
 * <a href="http://localhost:8082/mcp-chat?prompt=consume 10 of the inventory item with id 1">consume</a>
 * <a href="http://localhost:8082/mcp-chat?prompt=who ware you">who are you</a>
 * <a href="http://localhost:8082/mcp-chat?prompt=what time is it">time</a>
 *
 * @author Thomas Freese
 */
@RestController
@RequestMapping("mcp-chat")
public class McpChatController {
    private static final Logger LOGGER = LoggerFactory.getLogger(McpChatController.class);
    private final ChatClient chatClient;
    private final SyncMcpToolCallbackProvider syncMcpToolCallbackProvider;

    public McpChatController(final ChatClient.Builder chatClientBuilder,
                             @Qualifier("myToolCallbackProvider") final SyncMcpToolCallbackProvider syncMcpToolCallbackProvider,
                             @Value("classpath:prompts/mcp-system.st") final Resource systemPrompt) {
        super();

        this.chatClient = Objects.requireNonNull(chatClientBuilder, "chatClientBuilder required")
                .defaultSystem(systemPrompt)
                // .defaultOptions(chatOptions)
                .build();
        this.syncMcpToolCallbackProvider = Objects.requireNonNull(syncMcpToolCallbackProvider, "syncMcpToolCallbackProvider required");
    }

    // @RequestBody final String message
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public String chat(@RequestParam(value = "prompt") final String prompt) {
        LOGGER.info("Execute Prompt: {}", prompt);

        return chatClient.prompt()
                .user(prompt)
                .toolCallbacks(syncMcpToolCallbackProvider)
                .call().content();
    }
}
