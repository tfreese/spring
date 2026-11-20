package de.spring.ai.config.vectorstore;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.session.InMemorySessionRepository;
import org.springframework.ai.session.SessionRepository;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Volatile ChatMemoryRepository and VectorStore.
 *
 * @author Thomas Freese
 * @since 10.07.2025
 */
@Configuration
@Profile("memory")
public class DocumentVectorStoreConfigMemory {
    @Bean
    SessionRepository sessionRepository() {
        return InMemorySessionRepository.builder().build();
    }

    @Bean
    VectorStore vectorStore(final EmbeddingModel embeddingModel) {
        return SimpleVectorStore.builder(embeddingModel).build();
    }
}
