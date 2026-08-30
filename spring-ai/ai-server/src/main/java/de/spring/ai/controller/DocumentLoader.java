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
import org.springframework.ai.reader.ExtractedTextFormatter;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TextSplitter;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

/**
 * @author Thomas Freese
 * @since 29.06.2025
 */
final class DocumentLoader {
    private static final boolean ENRICH_METADATA = true;
    private static final Logger LOGGER = LoggerFactory.getLogger(DocumentLoader.class);

    static boolean isPriority(final String path) {
        // return path.contains("lastenhefte");
        return false;
    }

    /**
     * <a href="https://docs.spring.io/spring-ai/reference/api/etl-pipeline.html">etl-pipeline</a>
     *
     * @see PathMatchingResourcePatternResolver#getResources(String)
     */
    static List<Document> loadDocuments(final ChatModel chatModel, final List<String> locationPatterns) {
        final List<Resource> resources = getDocumentResources(locationPatterns);
        LOGGER.info("Processing resources: {}", resources.size());

        // final ExtractedTextFormatter extractedTextFormatter = ExtractedTextFormatter.defaults();
        final ExtractedTextFormatter extractedTextFormatter = ExtractedTextFormatter.builder()
                // .overrideLineSeparator("\n")
                // .withLeftAlignment(true)
                .build();

        final TextSplitter textSplitter = TokenTextSplitter.builder().build();

        final List<Document> documents = resources.stream()
                .filter(Objects::nonNull)
                .flatMap(resource -> readDocumentsFromResource(resource, extractedTextFormatter).stream())
                .flatMap(document -> splitDocument(document, textSplitter).stream())
                .flatMap(document -> enrichMetadata(chatModel, document).stream())
                .toList();

        LOGGER.info("Processing finished for {} Documents", documents.size());

        return documents;
    }

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

        // Or use custom templates.
        // final KeywordMetadataEnricher enricher = KeywordMetadataEnricher.builder(chatModel)
        //         .keywordsTemplate(YOUR_CUSTOM_TEMPLATE)
        //         .build();
        //
        // final List<Document> result = enricher.apply(documents);

        // final PromptTemplate template = new PromptTemplate(String.format(KeywordMetadataEnricher.KEYWORDS_TEMPLATE, 5));
        // final Prompt prompt = template.create(Map.of(KeywordMetadataEnricher.CONTEXT_STR_PLACEHOLDER, document.getText()));
        //
        // final String keywords = Optional.ofNullable(chatModel.call(prompt).getResult().getOutput().getText())
        //         .map(value -> value.replace(System.lineSeparator(), " "))
        //         .map(value -> PATTERN_NUMBERS_WITH_DOT_AND_SPACE.matcher(value).replaceAll(""))
        //         .map(value -> PATTERN_MULTI_SPACE.matcher(value).replaceAll(" "))
        //         .orElse(null);
        //
        // if (keywords != null) {
        //     document.getMetadata().put(KeywordMetadataEnricher.EXCERPT_KEYWORDS_METADATA_KEY, keywords);
        //
        //     LOGGER.info("Keywords for {}: {}", document.getMetadata().get("fileName"), keywords);
        // }

        final KeywordMetadataEnricher enricher = KeywordMetadataEnricher.builder(chatModel)
                .keywordCount(10)
                .build();

        return enricher.apply(List.of(document));
    }

    private static List<Resource> getDocumentResources(final List<String> locationPatterns) {
        final ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();

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

        return resources;
    }

    private static List<Document> readDocumentsFromResource(final Resource resource, final ExtractedTextFormatter extractedTextFormatter) {
        LOGGER.info("Loading documents from: {}", resource.getFilename());

        return new TikaDocumentReader(resource, extractedTextFormatter).read().stream()
                .map(document -> {
                    try {
                        // MetaData 'source'
                        // document.getMetadata().put("fileName", resource.getFilename());
                        document.getMetadata().put("priority", isPriority(resource.getFile().getAbsolutePath()));
                    }
                    catch (Exception ex) {
                        final String message = "Could not read file: %s".formatted(resource.getFilename());
                        LOGGER.error(message, ex.getMessage());
                    }

                    return document;
                })
                .toList();
    }

    private static List<Document> splitDocument(final Document document, final TextSplitter textSplitter) {
        LOGGER.info("Splitting document: {}", document.getMetadata().get("source"));

        return textSplitter.split(document).stream()
                .map(splittedDoc -> Document.builder()
                        .id(splittedDoc.getId())
                        .media(document.getMedia())
                        .metadata(splittedDoc.getMetadata())
                        .text(splittedDoc.getText())
                        .build())
                .toList();
    }

    private DocumentLoader() {
        super();
    }
}
