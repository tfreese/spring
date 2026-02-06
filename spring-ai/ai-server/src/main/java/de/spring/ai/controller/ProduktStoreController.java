package de.spring.ai.controller;

import java.time.LocalDateTime;
import java.util.Optional;

import de.spring.ai.Utils;
import de.spring.ai.tools.DateTimeTools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Thomas Freese
 */
@RestController
@RequestMapping("productStore")
public class ProduktStoreController {
    private static final Logger LOGGER = LoggerFactory.getLogger(ProduktStoreController.class);

    private final ChatClient chatClient;

    public ProduktStoreController(final ChatClient.Builder chatClientBuilder,
                                  @Value("classpath:prompts/systemprompt_entertaining.st") final Resource systemPrompt,
                                  @Qualifier("myToolCallbackProvider") final ToolCallbackProvider toolCallbackProvider) {
        super();

        this.chatClient = chatClientBuilder
                .clone()
                .defaultSystem(systemPrompt)
                .defaultTools(new DateTimeTools())
                .defaultToolCallbacks(toolCallbackProvider)
                // .defaultToolCallbacks(new FilteringToolCallbackProvider(toolCallbackProvider, Set.of("addProduct"))) // Or use MyMcpToolFilter.
                .defaultAdvisors(new SimpleLoggerAdvisor())
                .build();
    }

    @GetMapping("/chat")
    public String chat(@RequestParam(value = "prompt") final String prompt, @RequestParam(value = "id", required = false) final String conversationId) {
        LOGGER.info("Execute Prompt: {}", prompt);

        final LocalDateTime start = LocalDateTime.now();

        // String content = chatClient.prompt()
        //         .user(prompt)
        //         .call()
        //         .content();

        final ChatResponse chatResponse = chatClient.prompt()
                .user(prompt)
                .call()
                .chatResponse();

        if (chatResponse == null) {
            return "No ChatResponse";
        }

        final Usage usage = chatResponse.getMetadata().getUsage();

        String content = Optional.ofNullable(chatResponse.getResult()).map(Generation::getOutput).map(AssistantMessage::getText).orElse(null);

        // Do some presentation cosmetics for the content.
        if (content != null) {
            content = content
                    .replace(".", ".<br>")
                    .replace("?", "?<br>")
                    .replace("!", "!<br>")
                    .replace(System.lineSeparator(), "<br>");
        }

        if (content == null || content.isBlank()) {
            content = "No Content";
        }

        final String result = content;

        return Utils.toHtml(prompt, start, usage, sb -> sb.append(result));
    }
}
