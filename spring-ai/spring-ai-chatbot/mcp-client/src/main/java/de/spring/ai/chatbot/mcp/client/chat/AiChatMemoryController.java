package de.spring.ai.chatbot.mcp.client.chat;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import de.spring.ai.chatbot.mcp.client.ChatTools;
import de.spring.ai.chatbot.mcp.client.config.ChatConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;

/**
 * <a href="http://localhost:8082/ai-chat-memory?prompt=who ware you">who are you</a>
 * <a href="http://localhost:8082/ai-chat-memory/history">history</a>
 * <a href="http://localhost:8082/ai-chat-memory?prompt=what time is it in the GMT timezone">time</a>
 * <a href="http://localhost:8080/ai-chat-memory?prompt=who was till eulenspiegel">eulenspiegel</a><br>
 *
 * @author Thomas Freese
 */
@RestController
@RequestMapping("ai-chat-memory")
public class AiChatMemoryController {
    private static final Logger LOGGER = LoggerFactory.getLogger(AiChatMemoryController.class);

    public record MessageResponse(String conversationId, String prompt, String duration, String message) {
    }

    private final ChatClient chatClient;
    private final ChatMemoryRepository chatMemoryRepository;

    public AiChatMemoryController(final ChatClient.Builder chatClientBuilder,
                                  final ChatMemoryRepository chatMemoryRepository,
                                  @Value("classpath:prompts/chat-system.st") final Resource systemPrompt) {
        super();

        this.chatMemoryRepository = Objects.requireNonNull(chatMemoryRepository, "chatMemoryRepository required");

        final ChatMemory chatMemory = MessageWindowChatMemory.builder()
                .maxMessages(ChatConfig.MEMORY_MAX_MESSAGES)
                .chatMemoryRepository(chatMemoryRepository)
                .build();

        this.chatClient = Objects.requireNonNull(chatClientBuilder, "chatClientBuilder required")
                .defaultAdvisors(MessageChatMemoryAdvisor
                        .builder(chatMemory)
                        .build())
                .defaultSystem(systemPrompt)
                .defaultTools(new ChatTools())
                // .defaultOptions(chatOptions)
                .build();
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public MessageResponse chat(@RequestParam(value = "prompt") final String prompt, @RequestParam(value = "id", required = false) final String conversationId) {
        LOGGER.info("Execute Prompt: {}", prompt);

        final LocalDateTime start = LocalDateTime.now();

        // The following relates the conversation - ideally should be from the front end
        // when there is only one question in the conversation - then they are part of Default ID in the chat memory.
        //
        // UUID.randomUUID().toString()
        final String currentConversationId = conversationId == null ? RequestContextHolder.currentRequestAttributes().getSessionId() : conversationId;

        String content;

        if (conversationId == null) {
            content = chatClient.prompt()
                    .user(prompt)
                    .call()
                    .content();
        } else {
            content = chatClient.prompt()
                    .advisors(advisor -> advisor.param(ChatMemory.CONVERSATION_ID, currentConversationId))
                    .user(prompt)
                    .call()
                    .content();
        }

        final Duration duration = Duration.between(start, LocalDateTime.now());
        final String durationString = "%02d:%02d.%03d".formatted(duration.toMinutes(), duration.toSecondsPart(), duration.toMillisPart());

        if (content == null || content.isBlank()) {
            content = "no content";
        }

        return new MessageResponse(currentConversationId, prompt, durationString, content);
    }

    @DeleteMapping("/history/delete")
    public void deleteHistory() {
        findAllConversation().forEach(chatMemoryRepository::deleteByConversationId);
    }

    @GetMapping("/history")
    public List<String> findAllConversation() {
        return chatMemoryRepository.findConversationIds();
    }

    @GetMapping("/history/{id}")
    public List<Message> findConversationById(@PathVariable final String id) {
        return chatMemoryRepository.findByConversationId(id);
    }
}
