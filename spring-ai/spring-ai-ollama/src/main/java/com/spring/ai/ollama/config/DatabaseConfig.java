package com.spring.ai.ollama.config;

import com.spring.ai.ollama.vetorstore.JdbcVectorStore;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
public class DatabaseConfig {
    // @Bean
    // public DataSource getDataSource(@Value("${spring.datasource.username}") final String username,
    //                                 @Value("${spring.datasource.password}") final String password,
    //                                 final PasswordEncoder passwordEncoder) {
    //     final DataSourceBuilder dataSourceBuilder = DataSourceBuilder.create();
    //     dataSourceBuilder.username(username);
    //     dataSourceBuilder.password(password);
    //     // dataSourceBuilder.password(passwordEncoder.decode(password));
    //
    //     return dataSourceBuilder.build();
    // }

    // @Bean
    // PasswordEncoder passwordEncoder() {
    //     final Pbkdf2PasswordEncoder pbkdf2passwordEncoder = new Pbkdf2PasswordEncoder("mySecret", 16, 310_000,
    //             Pbkdf2PasswordEncoder.SecretKeyFactoryAlgorithm.PBKDF2WithHmacSHA512);
    //     pbkdf2passwordEncoder.setEncodeHashAsBase64(false);
    //
    //     final Map<String, PasswordEncoder> encoders = new HashMap<>();
    //     encoders.put("pbkdf2", pbkdf2passwordEncoder);
    //     encoders.put("bcrypt", new BCryptPasswordEncoder(10));
    //     encoders.put("noop", new PasswordEncoder() {
    //         @Override
    //         public String encode(final CharSequence rawPassword) {
    //             return rawPassword.toString();
    //         }
    //
    //         @Override
    //         public boolean matches(final CharSequence rawPassword, final String encodedPassword) {
    //             return rawPassword.toString().equals(encodedPassword);
    //         }
    //     });
    //
    //     return new DelegatingPasswordEncoder("noop", encoders);
    // }

    // @Bean
    // VectorStore vectorStore(final EmbeddingModel embeddingModel) {
    //     return SimpleVectorStore.builder(embeddingModel).build();
    // }

    @Bean
    VectorStore vectorStore(final EmbeddingModel embeddingModel, final JdbcTemplate jdbcTemplate) {
        return JdbcVectorStore.builder(embeddingModel).jdbcTemplate(jdbcTemplate).build();
    }
}
