package com.spring.ai.ollama.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.memory.InMemoryChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChatConfig {

    public static final int RAG_MAX_SIMILARITY_RESULTS = 5;
    // Chat memory configuration
    private static final int MEMORY_MAX_MESSAGES = 100;
    // RAG configuration
    private static final double RAG_MAX_THRESHOLD = 0.5;

    // Chatbot configuration
    private static final String SYSTEM_PROMPT = """
            You are a helpful AI assistant that helps people find information.
            If a user asks you a question in german language, answer only in german, don't give the answer in english in this case.
            """;

    @Bean
    ChatClient chatClient(final ChatClient.Builder builder, final VectorStore vectorStore, final ChatMemoryRepository chatMemoryRepository) {
        final ChatMemory chatMemory = MessageWindowChatMemory.builder()
                .maxMessages(MEMORY_MAX_MESSAGES)
                .chatMemoryRepository(chatMemoryRepository)
                .build();

        return builder.defaultSystem(SYSTEM_PROMPT)
                .defaultAdvisors(
                        MessageChatMemoryAdvisor.builder(chatMemory)
                                .order(1)
                                .build(),
                        QuestionAnswerAdvisor.builder(vectorStore)
                                .order(2)
                                .searchRequest(SearchRequest.builder()
                                        .similarityThresholdAll()
                                        .topK(RAG_MAX_SIMILARITY_RESULTS)
                                        .filterExpression("priority == true")
                                        .build()
                                )
                                .build(),
                        QuestionAnswerAdvisor.builder(vectorStore)
                                .order(3)
                                .searchRequest(SearchRequest.builder()
                                        .similarityThreshold(RAG_MAX_THRESHOLD)
                                        .topK(RAG_MAX_SIMILARITY_RESULTS)
                                        .build()
                                )
                                .build())
                // .defaultTools(new ChatTools())
                .build();
    }

    // @Bean
    // ChatMemoryRepository chatMemoryRepository(final DataSource dataSource, final PlatformTransactionManager txManager) {
    //     return JdbcChatMemoryRepository.builder()
    //             .dataSource(dataSource)
    //             .transactionManager(txManager)
    //             .build();
    // }

    @Bean
    ChatMemoryRepository chatMemoryRepository() {
        return new InMemoryChatMemoryRepository();
    }

    // @Bean
    // ChatMemoryRepository chatMemoryRepository(final Driver driver) {
    //     return new Neo4jChatMemoryRepository(Neo4jChatMemoryRepositoryConfig.builder()
    //             .withDriver(driver)
    //             .build());
    // }
}
