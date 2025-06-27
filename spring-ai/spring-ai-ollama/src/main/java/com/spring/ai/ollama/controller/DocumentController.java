package com.spring.ai.ollama.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import jakarta.annotation.Resource;

import com.spring.ai.ollama.config.ChatConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.document.MetadataMode;
import org.springframework.ai.model.transformer.KeywordMetadataEnricher;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TextSplitter;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.writer.FileDocumentWriter;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * http://localhost:8080/ai/documents/search?query=...&filter=</br>
 * http://localhost:8080/ai/documents/store
 */
@RestController
@RequestMapping("/ai/documents")
public class DocumentController {
    private static final boolean ENRICH_METADATA = false;
    private static final Logger LOGGER = LoggerFactory.getLogger(DocumentController.class);
    private static final String PRIORITY_FOLDER = "wiki";
    private static final boolean WRITE_DATABASE_TO_FILE = false;

    @Resource
    private ChatModel chatModel;

    @Resource
    private VectorStore vectorStore;

    @GetMapping("/search")
    public List<Document> search(@RequestParam(value = "query") final String query,
                                 @RequestParam(value = "filter") final String filter) {

        return vectorStore.similaritySearch(SearchRequest.builder()
                .similarityThresholdAll()
                .topK(ChatConfig.RAG_MAX_SIMILARITY_RESULTS)
                // .filterExpression(filter != null && !filter.isBlank() ? filter : "")
                .query(query)
                .build());
    }

    @GetMapping("/store")
    public String store() throws IOException {
        List<Document> documents = loadDocuments();

        // documents = cleanupText(documents);

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
                            .replaceAll("[^\\p{ASCII}]", "")
                            .replaceAll("\n+", "\n")
                            .replaceAll("\t+", "\t")
                            // .replace("\t", " ")
                            // .replace("\r", " ")
                            // .replace("\n", " ")
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
        if (!ENRICH_METADATA) {
            return documents;
        }

        LOGGER.info("Enriching metadata of documents.");

        // final List<Document> result = new KeywordMetadataEnricher(chatModel, 5).apply(documents);

        documents.stream()
                .parallel()
                .filter(doc -> doc.getText() != null)
                .forEach(doc -> {
                    // if (!Boolean.TRUE.equals(doc.getMetadata().get("priority"))) {
                    //     return;
                    // }

                    LOGGER.info("Enriching document: {}", doc.getMetadata().get("fileName"));

                    final PromptTemplate template = new PromptTemplate(String.format(KeywordMetadataEnricher.KEYWORDS_TEMPLATE, 5));
                    final Prompt prompt = template.create(Map.of(KeywordMetadataEnricher.CONTEXT_STR_PLACEHOLDER, doc.getText()));

                    final String keywords = chatModel.call(prompt).getResult().getOutput().getText();
                    doc.getMetadata().put("excerpt_keywords", keywords);

                    LOGGER.info("- keywords: {}", keywords);
                });

        LOGGER.info("Enriching done.");

        return documents;
    }

    private List<Document> loadDocuments() {
        LOGGER.info("Loading documents in knowledge base.");

        final PathMatchingResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();

        final List<Document> documents = new ArrayList<>();

        // file:/more_infos.txt
        // final List<String> locationPatterns = List.of("classpath*:static/doc-input/**/*.*");
        final List<String> locationPatterns = List.of("file:../linux-wiki/antora-wiki/wiki/modules/ROOT/pages/**/*.adoc");

        locationPatterns.stream()
                .map(locationPattern -> {
                    org.springframework.core.io.Resource[] resources = null;

                    try {
                        resources = resourcePatternResolver.getResources(locationPattern);
                    }
                    catch (IOException ex) {
                        LOGGER.error(ex.getMessage(), ex.getMessage());
                    }

                    return resources;
                })
                .filter(Objects::nonNull)
                .flatMap(Stream::of)
                .parallel()
                .forEach(resource -> {
                    try {
                        LOGGER.info("Loading document: {}", resource.getFilename());

                        for (Document document : new TikaDocumentReader(resource).read()) {
                            document.getMetadata().put("fileName", resource.getFilename());
                            document.getMetadata().put("priority", resource.getFile().getAbsolutePath().contains(PRIORITY_FOLDER));

                            documents.add(document);
                        }
                    }
                    catch (Exception ex) {
                        final String message = "Could not read file: %s".formatted(resource.getFilename());
                        LOGGER.error(message, ex.getMessage());
                    }
                });

        // for (String locationPattern : locationPatterns) {
        //     for (org.springframework.core.io.Resource resource : resourcePatternResolver.getResources(locationPattern)) {
        //         try {
        //             LOGGER.info("Loading document: {}", resource.getFilename());
        //
        //             for (Document document : new TikaDocumentReader(resource).read()) {
        //                 document.getMetadata().put("fileName", resource.getFilename());
        //                 document.getMetadata().put("priority", resource.getFile().getAbsolutePath().contains(PRIORITY_FOLDER));
        //
        //                 documents.add(document);
        //             }
        //         }
        //         catch (Exception ex) {
        //             final String message = "Could not read file: %s".formatted(resource.getFilename());
        //             LOGGER.error(message, ex.getMessage());
        //         }
        //     }
        // }

        LOGGER.info("Found {} documents.", documents.size());

        return documents;
    }

    private List<Document> splitDocuments(final List<Document> documents) {
        LOGGER.info("Tokenizing documents.");

        // final List<Document> result = new TokenTextSplitter().apply(documents);
        final TextSplitter textSplitter = new TokenTextSplitter();

        final List<Document> result = documents.stream()
                .parallel()
                .filter(doc -> doc.getText() != null)
                .map(textSplitter::split)
                .flatMap(List::stream)
                .toList();

        LOGGER.info("Tokenizing done. We now have {} split documents.", result.size());

        return result;
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
