package com.spring.ai.ollama.controller;

import java.util.ArrayList;
import java.util.List;

import jakarta.annotation.Resource;

import com.spring.ai.ollama.config.ChatConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.document.Document;
import org.springframework.ai.document.MetadataMode;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.writer.FileDocumentWriter;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * <a href="http://localhost:8080/ai/documents/search?query=...&filter=priority&#61;&#61;true">query</a><br>
 * <a href="http://localhost:8080/ai/documents/store">store</a>
 */
@RestController
@RequestMapping("/ai/documents")
public class DocumentController {
    static final boolean CLEANUP_TEXT = false;
    static final boolean ENRICH_METADATA = true;
    private static final Logger LOGGER = LoggerFactory.getLogger(DocumentController.class);
    private static final boolean WRITE_DATABASE_TO_FILE = false;

    static boolean isPriority(final String path) {
        // return path.contains("wiki");
        return false;
    }

    @Resource
    private ChatModel chatModel;

    @Resource
    private VectorStore vectorStore;

    /**
     * <pre>{@code
     * @RequestParam(value = "filter") final String filter
     * }</pre>
     */
    @GetMapping("/search")
    public List<Document> search(@RequestParam(value = "query") final String query) {
        return vectorStore.similaritySearch(SearchRequest.builder()
                .similarityThresholdAll()
                .topK(ChatConfig.RAG_MAX_SIMILARITY_RESULTS)
                // .filterExpression(filter != null && !filter.isBlank() ? filter : "")
                .query(query)
                .build());
    }

    @GetMapping("/store")
    public String store() {
        // file:/more_infos.txt
        final List<String> locationPatterns = new ArrayList<>();
        locationPatterns.add("classpath:doc-input/**/*.*");
        // final List<String> locationPatterns = List.of("classpath*:static/doc-input/**/*.*");

        final List<Document> documents = new DocumentLoader().loadDocuments(chatModel, locationPatterns);

        writeDocuments(documents);

        return "Documents processed and stored.";
    }

    private void writeDocuments(final List<Document> documents) {
        LOGGER.info("Storing documents in {} database.", WRITE_DATABASE_TO_FILE ? "file" : "vector");

        if (WRITE_DATABASE_TO_FILE) {
            final FileDocumentWriter writer = new FileDocumentWriter("documents-db.txt", true, MetadataMode.ALL, false);
            writer.accept(documents);
            LOGGER.info("Documents stored to file database.");

        }
        else {
            vectorStore.add(documents);
            LOGGER.info("Documents stored to vector database.");
        }
    }
}
