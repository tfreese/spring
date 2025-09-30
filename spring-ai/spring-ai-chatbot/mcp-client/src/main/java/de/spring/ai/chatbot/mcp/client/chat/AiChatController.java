package de.spring.ai.chatbot.mcp.client.chat;

import java.util.Objects;

import de.spring.ai.chatbot.mcp.client.ChatTools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * <a href="http://localhost:8082/ai-chat?prompt=who ware you">who are you</a>
 * <a href="http://localhost:8082/ai-chat?prompt=what date and time is it">time</a>
 *
 * @author Thomas Freese
 */
@RestController
@RequestMapping("ai-chat")
public class AiChatController {
    private static final Logger LOGGER = LoggerFactory.getLogger(AiChatController.class);

    private final ChatClient chatClient;

    public AiChatController(final ChatClient.Builder chatClientBuilder,
                            @Value("classpath:prompts/chat-system.st") final Resource systemPrompt) {
        super();

        this.chatClient = Objects.requireNonNull(chatClientBuilder, "chatClientBuilder required")
                .defaultSystem(systemPrompt)
                .defaultTools(new ChatTools())
                // .defaultOptions(chatOptions)
                .build();
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public String chat(@RequestParam(value = "prompt") final String prompt) {
        LOGGER.info("Execute Prompt: {}", prompt);

        return chatClient.prompt(prompt).call().content();
    }
}
