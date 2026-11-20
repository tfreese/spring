package de.spring.ai.controller;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Objects;

import de.spring.ai.tools.DateTimeTools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.session.DefaultSessionService;
import org.springframework.ai.session.SessionRepository;
import org.springframework.ai.session.SessionService;
import org.springframework.ai.session.advisor.SessionAdvisors;
import org.springframework.ai.session.advisor.SessionMemoryAdvisor;
import org.springframework.ai.session.compaction.TurnCountTrigger;
import org.springframework.ai.session.compaction.WindowCompactionStrategy;
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
    private final SessionService sessionService;

    public AiChatMemoryController(final ChatClient.Builder chatClientBuilder,
                                  final SessionRepository sessionRepository,
                                  @Value("classpath:prompts/systemprompt_entertaining.st") final Resource systemPrompt) {
        super();

        Objects.requireNonNull(sessionRepository, "sessionRepository required");

        sessionService = DefaultSessionService.builder()
                .sessionRepository(sessionRepository)
                .build();

        this.chatClient = Objects.requireNonNull(chatClientBuilder, "chatClientBuilder required")
                .defaultAdvisors(SessionAdvisors.builder(sessionService)
                        .compaction(
                                new TurnCountTrigger(20), // Auslöser: ab 20 Turns
                                WindowCompactionStrategy.builder().lastTurns(10).build() // Strategie: nur die letzten 10 behalten
                        )
                        .build()
                )
                .defaultSystem(systemPrompt)
                .defaultTools(new DateTimeTools())
                // .defaultOptions(chatOptions)
                .build();
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public MessageResponse chat(@RequestParam(value = "prompt") final String prompt, @RequestParam(value = "id", required = false) final String sessionId) {
        LOGGER.info("Execute Prompt: {}", prompt);

        final LocalDateTime start = LocalDateTime.now(ZoneId.systemDefault());

        // The following relates the conversation - ideally should be from the front end
        // when there is only one question in the conversation - then they are part of Default ID in the chat memory.
        //
        // UUID.randomUUID().toString()
        final String currentSessionId = sessionId == null ? RequestContextHolder.currentRequestAttributes().getSessionId() : sessionId;

        String content;

        if (currentSessionId == null) {
            content = chatClient.prompt()
                    .user(prompt)
                    .call()
                    .content();
        }
        else {
            content = chatClient.prompt()
                    .advisors(advisor -> advisor.param(SessionMemoryAdvisor.SESSION_ID_CONTEXT_KEY, currentSessionId))
                    .user(prompt)
                    .call()
                    .content();
        }

        final Duration duration = Duration.between(start, LocalDateTime.now(ZoneId.systemDefault()));
        final String durationString = "%02d:%02d.%03d".formatted(duration.toMinutes(), duration.toSecondsPart(), duration.toMillisPart());

        if (content == null || content.isBlank()) {
            content = "no content";
        }

        return new MessageResponse(currentSessionId, prompt, durationString, content);
    }

    @DeleteMapping("/history/delete/{id}")
    public void deleteHistory(@PathVariable final String sessionId) {
        sessionService.delete(sessionId);
    }

    // @GetMapping("/history")
    // public List<String> findAllSessions() {
    //     return null;
    // }

    @GetMapping("/history/{sessionId}")
    public List<Message> findSessionById(@PathVariable final String sessionId) {
        return sessionService.getMessages(sessionId);
    }
}
