package com.spring.ai.ollama;

import jakarta.annotation.Resource;

import com.spring.ai.ollama.controller.DocumentController;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
//(exclude = {Neo4jVectorStoreAutoConfiguration.class})
public class Application {
    public static void main(final String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Resource
    private DocumentController documentController;

    @Resource
    private VectorStore vectorStore;

    // @Bean
    // // @DependsOn({"dataSourceInitializer"})
    // public CommandLineRunner loadDocuments() {
    //     return args -> {
    //         // if (vectorStore instanceof JdbcVectorStore jvs) {
    //         //     jvs.loadAll();
    //         //
    //         //     if (jvs.size() > 0) {
    //         //         return;
    //         //     }
    //         // }
    //
    //         documentController.store();
    //     };
    // }
}
