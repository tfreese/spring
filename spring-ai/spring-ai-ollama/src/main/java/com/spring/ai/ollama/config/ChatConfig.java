package com.spring.ai.ollama.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChatConfig {

    // Chat memory configuration
    private static final int MEMORY_MAX_MESSAGES = 100;

    private static final int RAG_MAX_SIMILARITY_RESULTS = 5;

    // RAG configuration
    private static final double RAG_MAX_THRESHOLD = 0.02;

    // Chatbot configuration
    private static final String SYSTEM_PROMPT = """
            You are a helpful AI assistant that helps people find information.
            If a user asks you a question in german language, answer only in german, don't give the answer in english in this case.
            """;

    @Bean
    ChatClient chatClient(final ChatClient.Builder builder, final VectorStore vectorStore) {
        final ChatMemory chatMemory = MessageWindowChatMemory.builder().maxMessages(MEMORY_MAX_MESSAGES).build();

        return builder.defaultSystem(SYSTEM_PROMPT)
                .defaultAdvisors(
                        MessageChatMemoryAdvisor.builder(chatMemory).build(),
                        QuestionAnswerAdvisor.builder(vectorStore)
                                .searchRequest(SearchRequest.builder()
                                        .similarityThreshold(RAG_MAX_THRESHOLD)
                                        .topK(RAG_MAX_SIMILARITY_RESULTS)
                                        .build())
                                .build())
                .build();
    }
}
