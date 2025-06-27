// Created: 27.06.2025
package com.spring.ai;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.Normalizer;

import org.apache.tika.Tika;
import org.apache.tika.config.TikaConfig;
import org.apache.tika.exception.TikaException;
import org.apache.tika.io.TikaInputStream;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MediaType;
import org.apache.tika.mime.MimeTypes;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.sax.BodyContentHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

/**
 * @author Thomas Freese
 */
public final class TikaDemo {
    private static final Logger LOGGER = LoggerFactory.getLogger(TikaDemo.class);

    // dependencySet(group: "org.apache.tika", version: "3.2.0") {
    //     entry("tika-core")
    //     entry("tika-parsers-standard-package")
    // }

    public static void main(final String[] args) {
        final Path path = Path.of("spring-ai/spring-ai-ollama/README.adoc");

        try {
            final TikaConfig tikaConfig = TikaConfig.getDefaultConfig();
            final Tika tika = new Tika(tikaConfig);

            LOGGER.info("detectMimeType1; {}", detectMimeType1(tika, path));
            LOGGER.info("detectMimeType2; {}", detectMimeType2(path));
            LOGGER.info("detectMimeType3; {}", detectMimeType3(tika, path));

            LOGGER.info("readContent1; {}", flattenToAscii(readContent1(tika, path)));
            LOGGER.info("readContent2; {}", flattenToAscii(readContent2(path)));
        }
        catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
        }
    }

    private static MediaType detectMimeType1(final Tika tika, final Path path) throws IOException {
        return MediaType.parse(tika.detect(path));
    }

    private static MediaType detectMimeType2(final Path path) throws IOException {
        final Metadata metadata = new Metadata();
        final MimeTypes mimeTypes = MimeTypes.getDefaultMimeTypes();

        try (InputStream inputStream = TikaInputStream.get(path, metadata)) {
            return mimeTypes.detect(inputStream, metadata);
        }
    }

    private static MediaType detectMimeType3(final Tika tika, final Path path) throws IOException {
        try (InputStream inputStream = Files.newInputStream(path)) {
            // Without a Stream who supports mark() and reset(), the Data will be read incomplete,
            // because Tika needs to read part of the stream, to detect the MimeType.
            // mark() and reset() are called by Tika automatically.
            InputStream markSupportedInputStream = null;

            if (inputStream.markSupported()) {
                markSupportedInputStream = inputStream;
            }
            else {
                markSupportedInputStream = new BufferedInputStream(inputStream, 8_192);
            }

            return MediaType.parse(tika.detect(markSupportedInputStream));
        }
    }

    /**
     * Replacement for REGEX, better Performance for large Strings.
     * <pre>{@code
     * value
     *      .replaceAll("[^\\p{ASCII}]", "")
     *      .replaceAll("\n+", "\n")
     * ;
     * }</pre>
     */
    private static String flattenToAscii(final String value) {
        final String normalized = Normalizer.normalize(value, Normalizer.Form.NFD);
        final StringBuilder sb = new StringBuilder();

        for (int i = 0; i < normalized.length(); ++i) {
            final char c = normalized.charAt(i);

            if (c <= '\u007F') {
                sb.append(c);
            }
        }

        return sb.toString()
                .replaceAll(System.lineSeparator() + "+", System.lineSeparator())
                ;
    }

    private static String readContent1(final Tika tika, final Path path) throws TikaException, IOException {
        return tika.parseToString(path);
    }

    private static String readContent2(final Path path) throws IOException, TikaException, SAXException {
        final Parser parser = new AutoDetectParser();
        final ContentHandler handler = new BodyContentHandler(-1);
        final Metadata metadata = new Metadata();
        final ParseContext context = new ParseContext();
        context.set(Parser.class, parser);

        try (InputStream inputStream = Files.newInputStream(path)) {
            parser.parse(inputStream, handler, metadata, context);
        }

        return handler.toString();
    }

    private TikaDemo() {
        super();
    }
}
