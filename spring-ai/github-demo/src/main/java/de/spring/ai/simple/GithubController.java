// Created: 17.07.2026
package de.spring.ai.simple;

import java.util.Objects;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Thomas Freese
 */
@RestController
public class GithubController {
    // private static final Logger LOGGER = LoggerFactory.getLogger(GithubController.class);

    private final ChatClient chatClient;

    // public AiController(final ChatClient.Builder chatClientBuilder) {
    //     super();
    //
    //     chatClient = chatClientBuilder
    //             .defaultSystem("Du bist ein hilfreicher Assistent auf GitHub.")
    //             .defaultAdvisors(new SimpleLoggerAdvisor())
    //             .build();
    // }
    public GithubController(final ChatClient gitHubChatClient) {
        super();

        this.chatClient = Objects.requireNonNull(gitHubChatClient, "chatClient required");
    }

    @GetMapping("/github")
    public String generate(@RequestParam(value = "message", defaultValue = "Erzähle mir einen kurzen Entwickler-Witz.") final String message) {
        return chatClient.prompt()
                // .options(OpenAiChatOptions.builder().model("openai/gpt-4o"))
                .user(message)
                .call()
                .content();

        // final ChatResponse chatResponse = chatClient.prompt()
        //         .user(message)
        //         .call()
        //         .chatResponse();
        //
        // final Optional<ChatResponseMetadata> chatResponseMetadata = Optional.ofNullable(chatResponse).map(ChatResponse::getMetadata);
        // chatResponseMetadata.ifPresent(metadata -> LOGGER.info("Chat Response Metadata: {}", metadata));
        //
        // return Optional.ofNullable(chatResponse)
        //         .map(ChatResponse::getResult)
        //         .map(Generation::getOutput)
        //         .map(AbstractMessage::getText)
        //         .orElse("Keine Antwort erhalten.");
    }
}
