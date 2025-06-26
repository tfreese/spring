package com.spring.ai.ollama;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import javax.sql.DataSource;

import jakarta.annotation.Resource;

import com.spring.ai.ollama.controller.DocumentController;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.DataSourceInitializer;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;

@SpringBootApplication
public class Application {
    public static void main(final String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Resource
    private DocumentController documentController;

    @Bean
    @DependsOn({"dataSourceInitializer"})
    public CommandLineRunner loadDocuments() {
        return args -> documentController.store();
    }

    @Bean
    DataSourceInitializer dataSourceInitializer(final DataSource dataSource) throws IOException {
        final org.springframework.core.io.Resource resourceJdbcChatMemoryRepository =
                new ClassPathResource("/org/springframework/ai/chat/memory/repository/jdbc/schema-hsqldb.sql");

        final String sql = resourceJdbcChatMemoryRepository.getContentAsString(StandardCharsets.UTF_8)
                .replace("CREATE TABLE", "CREATE TABLE IF NOT EXISTS")
                .replace("CREATE INDEX", "CREATE INDEX IF NOT EXISTS")
                .replace("ADD CONSTRAINT", "ADD CONSTRAINT IF NOT EXISTS");

        final ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
        populator.addScript(new ByteArrayResource(sql.getBytes(StandardCharsets.UTF_8)));
        // populator.execute(dataSource);

        final DataSourceInitializer dataSourceInitializer = new DataSourceInitializer();
        dataSourceInitializer.setDataSource(dataSource);
        dataSourceInitializer.setDatabasePopulator(populator);

        return dataSourceInitializer;
    }
}
