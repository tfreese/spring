// Created: 29.06.2025
package com.spring.ai.ollama.controller;

import static com.spring.ai.ollama.controller.DocumentController.ENRICH_METADATA;
import static com.spring.ai.ollama.controller.DocumentController.PRIORITY_FOLDER;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
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
public final class DocumentLoader {
    private static final Logger LOGGER = LoggerFactory.getLogger(DocumentLoader.class);

    private static final Pattern PATTERN_3_LINE_BREAK = Pattern.compile(System.lineSeparator() + "{3,}");
    private static final Pattern PATTERN_MULTI_SPACE = Pattern.compile(" +");
    private static final Pattern PATTERN_MULTI_TAB = Pattern.compile("\t+");
    private static final Pattern PATTERN_NUMBERS_WITH_DOT_AND_SPACE = Pattern.compile("\\d\\. ");
    // private static final Pattern PATTERN_ONLY_ASCII = Pattern.compile("[^\\\\p{ASCII}]");

    private static Document cleanupDocument(final Document document) {
        final String text = Optional.ofNullable(document.getText())
                // .map(value -> PATTERN_ONLY_ASCII.matcher(value).replaceAll(""))
                .map(value -> PATTERN_3_LINE_BREAK.matcher(value).replaceAll(System.lineSeparator() + System.lineSeparator()))
                .map(value -> PATTERN_MULTI_TAB.matcher(value).replaceAll("\t"))
                .map(value -> PATTERN_MULTI_SPACE.matcher(value).replaceAll(" "))
                // .replaceAll("\n+", "\n")
                // .replaceAll("\t+", "\t")
                // .replace("\t", " ")
                // .replace("\r", " ")
                // .replace("\n", " ")
                // .replaceAll(" +", " ")
                .orElse("");

        return Document.builder()
                .id(document.getId())
                .media(document.getMedia())
                .metadata(document.getMetadata())
                .text(text)
                .build()
                ;
    }

    private static Document enrichMetadata(final ChatModel chatModel, final Document document) {
        if (!ENRICH_METADATA) {
            return document;
        }

        if (document.getText() == null) {
            LOGGER.warn("Document without Text: {}", document.getMetadata().get("fileName"));

            return document;
        }

        LOGGER.info("Enriching metadata of document: {}", document.getMetadata().get("fileName"));

        // final List<Document> result = new KeywordMetadataEnricher(chatModel, 5).apply(documents);

        // if (!Boolean.TRUE.equals(doc.getMetadata().get("priority"))) {
        //     return;
        // }

        final PromptTemplate template = new PromptTemplate(String.format(KeywordMetadataEnricher.KEYWORDS_TEMPLATE, 5));
        final Prompt prompt = template.create(Map.of(KeywordMetadataEnricher.CONTEXT_STR_PLACEHOLDER, document.getText()));

        final String keywords = Optional.ofNullable(chatModel.call(prompt).getResult().getOutput().getText())
                .map(value -> value.replace(System.lineSeparator(), " "))
                .map(value -> PATTERN_NUMBERS_WITH_DOT_AND_SPACE.matcher(value).replaceAll(""))
                .map(value -> PATTERN_MULTI_SPACE.matcher(value).replaceAll(" "))
                .orElse(null);

        if (keywords != null) {
            document.getMetadata().put("excerpt_keywords", keywords);

            LOGGER.info("Keywords for {}: {}", document.getMetadata().get("fileName"), keywords);
        }

        return document;
    }

    private static Stream<Document> readDocumentsFromResource(final Resource resource) {
        LOGGER.info("Loading documents from: {}", resource.getFilename());

        return new TikaDocumentReader(resource).read().stream()
                .map(document -> {
                    try {
                        document.getMetadata().put("fileName", resource.getFilename());
                        document.getMetadata().put("priority", resource.getFile().getAbsolutePath().contains(PRIORITY_FOLDER));
                    }
                    catch (Exception ex) {
                        final String message = "Could not read file: %s".formatted(resource.getFilename());
                        LOGGER.error(message, ex.getMessage());
                    }

                    return document;
                });
    }

    private static Stream<Document> splitDocument(final Document document, final TextSplitter textSplitter) {
        LOGGER.info("Splitting document: {}", document.getMetadata().get("fileName"));

        return textSplitter.split(document).stream()
                .map(splittedDoc -> Document.builder()
                        .id(splittedDoc.getId())
                        .media(document.getMedia())
                        .metadata(splittedDoc.getMetadata())
                        .text(splittedDoc.getText())
                        .build());
    }

    /**
     * @see PathMatchingResourcePatternResolver#getResources(String)
     */
    List<Document> loadDocuments(final ChatModel chatModel, final List<String> locationPatterns) {
        final PathMatchingResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();

        final List<Resource> resources = new ArrayList<>();

        for (String locationPattern : locationPatterns) {
            LOGGER.info("Loading resources from: {}", locationPattern);

            try {
                resources.addAll(Arrays.asList(resourcePatternResolver.getResources(locationPattern)));
            }
            catch (IOException ex) {
                LOGGER.error(ex.getMessage(), ex.getMessage());
            }
        }

        resources.sort(Comparator.comparing(Resource::getFilename));

        LOGGER.info("Processing resources: {}", resources.size());

        // final List<Document> result = new TokenTextSplitter().apply(documents);
        final TextSplitter textSplitter = new TokenTextSplitter();

        final List<Document> documents = resources.stream()
                // .parallel()
                .filter(Objects::nonNull)
                .flatMap(DocumentLoader::readDocumentsFromResource)
                .map(DocumentLoader::cleanupDocument)
                .flatMap(document -> splitDocument(document, textSplitter))
                .map(document -> enrichMetadata(chatModel, document))
                .toList();

        LOGGER.info("Processing finished for {} Documents", documents.size());

        return documents;
    }
}
