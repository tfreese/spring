package com.spring.ai.ollama.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import jakarta.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.document.Document;
import org.springframework.ai.document.MetadataMode;
import org.springframework.ai.model.transformer.KeywordMetadataEnricher;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.writer.FileDocumentWriter;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ai/documents")
public class DocumentController {
    private static final Logger LOGGER = LoggerFactory.getLogger(DocumentController.class);

    private static final boolean WRITE_DATABASE_TO_FILE = false;

    @Resource
    private ChatModel chatModel;

    @Resource
    private VectorStore vectorStore;

    @GetMapping("/search")
    public List<Document> search(@RequestParam(value = "query") final String query) {
        return vectorStore.similaritySearch(query);
    }

    @GetMapping("/store")
    public String store() throws IOException {
        List<Document> documents = upload();

        documents = cleanupText(documents);

        documents = splitDocuments(documents);

        documents = enrichMetadata(documents);

        writeDocuments(documents);

        return "Documents processed and stored.";
    }

    private List<Document> cleanupText(final List<Document> documents) {
        LOGGER.info("Cleanup Document text.");

        final List<Document> result = documents.stream()
                .parallel()
                .filter(doc -> doc.getText() != null)
                .filter(doc -> !doc.getText().isBlank())
                .map(doc -> {
                    final String text = doc.getText()
                            .replace("\t", " ")
                            .replace("\r", " ")
                            .replace("\n", " ")
                            .replaceAll(" +", " ");

                    return Document.builder()
                            .id(doc.getId())
                            .media(doc.getMedia())
                            .metadata(doc.getMetadata())
                            .score(doc.getScore())
                            .text(text)
                            .build()
                            ;
                })
                .toList();

        LOGGER.info("Cleanup done.");

        return result;
    }

    private List<Document> enrichMetadata(final List<Document> documents) {
        LOGGER.info("Enriching metadata of documents.");

        final List<Document> result = new KeywordMetadataEnricher(chatModel, 5).apply(documents);

        LOGGER.info("Enriching done.");

        return result;
    }

    private List<Document> splitDocuments(final List<Document> documents) {
        LOGGER.info("Tokenizing documents.");

        final List<Document> result = new TokenTextSplitter().apply(documents);

        LOGGER.info("Tokenizing done. We now have {} split documents.", result.size());

        return result;
    }

    private List<Document> upload() throws IOException {
        LOGGER.info("Loading documents in knowledge base.");

        final PathMatchingResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();

        final List<Document> documents = new ArrayList<>();

        for (org.springframework.core.io.Resource resource : resourcePatternResolver.getResources("classpath*:**/static/doc-input/*.*")) {
            final TikaDocumentReader tikaDocumentReader = new TikaDocumentReader(resource);
            documents.addAll(tikaDocumentReader.read());
        }

        LOGGER.info("Found {} documents.", documents.size());

        return documents;
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
