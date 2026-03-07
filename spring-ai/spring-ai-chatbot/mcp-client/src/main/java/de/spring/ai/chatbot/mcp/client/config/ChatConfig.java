package de.spring.ai.chatbot.mcp.client.config;

import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * <a href="https://spring.io/blog/2025/04/14/spring-ai-prompt-engineering-patterns">spring-ai-prompt-engineering-patterns</a>
 *
 * @author Thomas Freese
 */
@Configuration
@Profile("disabled")
public class ChatConfig {
    public static final int RAG_MAX_SIMILARITY_RESULTS = 5;

    /**
     * Chat memory configuration
     */
    public static final int MEMORY_MAX_MESSAGES = 10;

    /**
     * RAG configuration (0-1)
     */
    public static final double RAG_MAX_THRESHOLD = 0.5D;

    /**
     * See application.yml.
     */
    @Bean
    ChatOptions chatOptions() {
        // OpenAiChatOptions.builder()
        return ChatOptions.builder()
                .model("llama3.2")

                // Temperature controls the randomness or "creativity" of the model's response.
                .temperature(0.5D)

                // Limits token selection to the K most likely next tokens.
                // Higher values (e.g., 40-50) introduce more diversity.
                .topK(40)

                // (nucleus sampling): Dynamically selects from the smallest set of tokens whose cumulative probability exceeds P.
                // Values like 0.8-0.95 are common.
                .topP(0.8D)

                // The maxTokens parameter limits how many tokens (word pieces) the model can generate in its response.
                .maxTokens(1024)

                .build();
    }
}
