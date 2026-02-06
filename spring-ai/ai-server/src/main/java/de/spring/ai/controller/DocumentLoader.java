// Created: 29.06.2025
package de.spring.ai.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.document.Document;
import org.springframework.ai.model.transformer.KeywordMetadataEnricher;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TextSplitter;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

/**
 * @author Thomas Freese
 */
final class DocumentLoader {
    private static final Logger LOGGER = LoggerFactory.getLogger(DocumentLoader.class);

    private static final boolean ENRICH_METADATA = true;

    private static List<Document> enrichMetadata(final ChatModel chatModel, final Document document) {
        if (!ENRICH_METADATA) {
            return List.of(document);
        }

        if (document.getText() == null) {
            LOGGER.warn("Document without Text: {} - {}/{}",
                    document.getMetadata().get("source"),
                    document.getMetadata().get("chunk_index"),
                    document.getMetadata().get("total_chunks"));

            return List.of();
        }

        LOGGER.info("Enriching metadata of document: {} - {}/{}",
                document.getMetadata().get("source"),
                document.getMetadata().get("chunk_index"),
                document.getMetadata().get("total_chunks"));

        final KeywordMetadataEnricher enricher = KeywordMetadataEnricher.builder(chatModel)
                .keywordCount(5)
                .build();

        return enricher.apply(List.of(document));
    }

    private static List<Document> readDocumentsFromResource(final Resource resource) {
        LOGGER.info("Loading documents from: {}", resource.getFilename());

        return new TikaDocumentReader(resource).read();
    }

    private static List<Document> splitDocument(final Document document, final TextSplitter textSplitter) {
        LOGGER.info("Splitting document: {}", document.getMetadata().get("source"));

        return textSplitter.split(document);
    }

    /**
     * <a href="https://docs.spring.io/spring-ai/reference/api/etl-pipeline.html">etl-pipeline</a>
     *
     * @see PathMatchingResourcePatternResolver#getResources(String)
     */
    List<Document> loadDocuments(final ChatModel chatModel, final List<String> locationPatterns) {
        final List<Resource> resources = getDocumentResources(locationPatterns);
        LOGGER.info("Processing resources: {}", resources.size());

        final TextSplitter textSplitter = TokenTextSplitter.builder().build();

        final List<Document> documents = resources.stream()
                .filter(Objects::nonNull)
                .flatMap(document -> readDocumentsFromResource(document).stream())
                .filter(Objects::nonNull)
                .flatMap(document -> splitDocument(document, textSplitter).stream())
                .filter(Objects::nonNull)
                .flatMap(document -> enrichMetadata(chatModel, document).stream())
                .filter(Objects::nonNull)
                .toList();

        LOGGER.info("Processing finished for {} Documents", documents.size());

        return documents;
    }

    private List<Resource> getDocumentResources(final List<String> locationPatterns) {
        final PathMatchingResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();

        final List<Resource> resources = new ArrayList<>();

        for (String locationPattern : locationPatterns) {
            LOGGER.info("Loading resources from: {}", locationPattern);

            try {
                resources.addAll(Arrays.asList(resourcePatternResolver.getResources(locationPattern)));
            } catch (IOException ex) {
                LOGGER.error(ex.getMessage(), ex.getMessage());
            }
        }

        resources.sort(Comparator.comparing(Resource::getFilename));

        return resources;
    }
}
