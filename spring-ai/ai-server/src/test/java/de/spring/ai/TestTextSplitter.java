// Created: 11.02.2026
package de.spring.ai;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import de.spring.ai.config.vectorstore.SentenceAwareTextSplitter;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TextSplitter;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

/**
 * @author Thomas Freese
 */
// @SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE, classes = AiServerApplication.class)
// @ActiveProfiles("test")
class TestTextSplitter {
    private static final Logger LOGGER = LoggerFactory.getLogger(TestTextSplitter.class);

    private static final List<Resource> RESOURCES = new ArrayList<>();

    @AfterAll
    static void afterAll() {
        RESOURCES.clear();
    }

    @BeforeAll
    static void beforeAll() throws IOException {
        final List<String> locationPatterns = new ArrayList<>();
        locationPatterns.add("classpath*:doc-input/**/*.pdf");

        final PathMatchingResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();

        for (String locationPattern : locationPatterns) {
            RESOURCES.addAll(Arrays.asList(resourcePatternResolver.getResources(locationPattern)));
        }

        RESOURCES.sort(Comparator.comparing(Resource::getFilename));
    }

    @Test
    void testSentenceAwareTextSplitter() {
        final TextSplitter textSplitter = new SentenceAwareTextSplitter(800);

        final List<Document> documents = createDocuments(RESOURCES, textSplitter);

        for (Document document : documents) {
            final String text = document.getText();
            assertNotNull(text);

            assertTrue(text.endsWith(".") || text.endsWith("!") || text.endsWith("?"), text);
        }
    }

    @Test
    void testTokenTextSplitter() {
        // Configure TokenTextSplitter in a way that conserves sentence size.
        final TextSplitter textSplitter = TokenTextSplitter.builder()
                .withChunkSize(800)              // Size in Tokens.
                .withMinChunkSizeChars(300)      // Minimum number of characters up to a "snap" point.
                .withMinChunkLengthToEmbed(20)   // Ignore very short crumbs.
                .withMaxNumChunks(10_000)
                .withKeepSeparator(true)         // Keep separators ('.', '?', '!', '\n').
                .build();

        createDocuments(RESOURCES, textSplitter);
    }

    private List<Document> createDocuments(final List<Resource> resources, final TextSplitter textSplitter) {
        final List<Document> documents = resources.stream()
                .filter(Objects::nonNull)
                .flatMap(resource -> new TikaDocumentReader(resource).read().stream())
                .flatMap(document -> textSplitter.split(document).stream())
                .toList();

        assertNotNull(documents);
        assertFalse(documents.isEmpty());

        int maxLength = 0;

        for (Document document : documents) {
            assertNotNull(document);

            final String text = document.getText();
            assertNotNull(text);

            maxLength = Math.max(maxLength, text.length());
        }

        assertTrue(maxLength > 0);

        LOGGER.info("Max length: {}", maxLength);

        return documents;
    }
}
