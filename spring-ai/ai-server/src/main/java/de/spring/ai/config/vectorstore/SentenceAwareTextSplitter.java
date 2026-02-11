// Created: 11.02.2026
package de.spring.ai.config.vectorstore;

import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.function.UnaryOperator;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.knuddels.jtokkit.Encodings;
import com.knuddels.jtokkit.api.Encoding;
import com.knuddels.jtokkit.api.EncodingType;
import org.jspecify.annotations.Nullable;
import org.springframework.ai.transformer.splitter.TextSplitter;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;

/**
 * {@link TextSplitter} that keep complete sentences.<br>
 * See: com.alibaba.cloud.ai.transformer.splitter.SentenceSplitter
 *
 * @author Thomas Freese
 * @see TokenTextSplitter
 */
public final class SentenceAwareTextSplitter extends TextSplitter {
    /**
     * Last chance to edit the single sentences before they are aggregated to {@link org.springframework.ai.document.Document}s.
     */
    public static class DefaultSentenceTransformer implements UnaryOperator<List<String>> {
        @Override
        public List<String> apply(final List<String> sentences) {
            final List<String> chunks = sentences.stream().filter(Objects::nonNull).collect(Collectors.toCollection(ArrayList::new));
            final List<String> result = new ArrayList<>(chunks.size());

            if (chunks.size() > 1) {
                // Check for false positives:
                // Example:
                // "In the 16. Century" ist split into
                // [0] "In the 16."
                // [1] "Century"
                while (!chunks.isEmpty()) {
                    final String chunk = chunks.removeFirst();
                    String chunkNext = null;

                    if (!chunks.isEmpty()) {
                        chunkNext = chunks.removeFirst().strip();
                    }

                    if (chunkNext != null) {
                        if (chunkNext.toLowerCase().startsWith("century") || chunkNext.toLowerCase().startsWith("jahrhundert")) {
                            String c = chunk.strip();

                            if (c.endsWith(".")) {
                                c = c.substring(0, c.length() - 1);
                            }

                            // Get last Word.
                            final int lastIndex = c.lastIndexOf(' ');

                            if (lastIndex > 0) {
                                c = c.substring(lastIndex).strip();
                            }

                            if (c.codePoints().allMatch(Character::isDigit)) {
                                if (chunk.endsWith(".")) {
                                    result.add(chunk + " " + chunkNext);
                                }
                                else {
                                    result.add(chunk + ". " + chunkNext);
                                }

                                continue;
                            }
                        }
                    }

                    result.add(chunk);
                    result.add(chunkNext);
                }
            }
            else {
                result.addAll(chunks);
            }

            // Last cosmetics.
            return result.stream()
                    .filter(Objects::nonNull)
                    .map(chunk -> {
                        final String sentence;

                        if (chunk.endsWith(".\"")) {
                            sentence = chunk.substring(0, chunk.length() - 2) + "\".";
                        }
                        else if (chunk.endsWith("?\"")) {
                            sentence = chunk.substring(0, chunk.length() - 2) + "\"?";
                        }
                        else if (chunk.endsWith("!\"")) {
                            sentence = chunk.substring(0, chunk.length() - 2) + "\"!";
                        }
                        else {
                            sentence = chunk;
                        }

                        return sentence.strip();
                    })
                    .toList();
        }
    }

    /**
     * Normalize the Raw-Text.
     */
    public static class DefaultTextNormalizer implements UnaryOperator<String> {
        protected static final Pattern PATTERN_DIFFERENT_BREAK = Pattern.compile("(\\r\\n|\\r)");
        protected static final Pattern PATTERN_MULTIPLE_BREAKS = Pattern.compile("(\\r\\n|\\r|\\n){2,}");
        protected static final Pattern PATTERN_MULTIPLE_SPACES = Pattern.compile(" {2,}");
        protected static final Pattern PATTERN_TABS = Pattern.compile("\\t");

        @Override
        public String apply(final String text) {
            String txt = text.strip();

            // Adjust some things.
            txt = txt
                    .replace("( ", "(")
                    .replace(" )", ")")
                    .replace("{ ", "{")
                    .replace(" }", "}")
                    .replace(":", ": ")
                    .replace("»", "\"")
                    .replace("«", "\"")
            ;

            // Replace Tabs.
            txt = PATTERN_TABS.matcher(txt).replaceAll(" ");

            // Normalize LineBreaks.
            txt = PATTERN_DIFFERENT_BREAK.matcher(txt).replaceAll("\n");

            // Replace multiple LineBreaks.
            txt = PATTERN_MULTIPLE_BREAKS.matcher(txt).replaceAll("\n");

            // Replace all LineBreaks.
            txt = txt.replace("\n", " ");

            // Replace multiple Spaces.
            txt = PATTERN_MULTIPLE_SPACES.matcher(txt).replaceAll(" ");

            // Remove leading LineBreak.
            if (txt.startsWith("\n")) {
                txt = txt.substring(1);
            }

            // Remove trailing LineBreak.
            if (txt.endsWith("\n")) {
                txt = txt.substring(0, txt.length() - 1);
            }

            return txt.strip();
        }
    }

    private static List<String> splitIntoSentences(final String text, final Locale locale) {
        final BreakIterator breakIterator = BreakIterator.getSentenceInstance(locale);
        breakIterator.setText(text);

        final List<String> sentences = new ArrayList<>();

        int start = breakIterator.first();

        for (int end = breakIterator.next(); end != BreakIterator.DONE; start = end, end = breakIterator.next()) {
            sentences.add(text.substring(start, end).strip());
        }

        return sentences;
    }

    private final Encoding encoding;
    private final UnaryOperator<List<String>> sentenceTransformer;
    private final UnaryOperator<String> textNormalizer;
    private final int tokenCount;

    /**
     * Default tokenCount: 800
     */
    public SentenceAwareTextSplitter(final int tokenCount) {
        this(tokenCount, new DefaultTextNormalizer(), new DefaultSentenceTransformer());
    }

    /**
     * @param tokenCount; see TokenTextSplitter#DEFAULT_CHUNK_SIZE
     * @param textNormalizer; Normalize the Raw-Text.
     * @param sentenceTransformer; Last chance to edit the TextSplitter Result.
     */
    public SentenceAwareTextSplitter(final int tokenCount, @Nullable final UnaryOperator<String> textNormalizer, @Nullable final UnaryOperator<List<String>> sentenceTransformer) {
        super();

        this.tokenCount = tokenCount;

        this.encoding = Encodings.newLazyEncodingRegistry().getEncoding(EncodingType.CL100K_BASE);
        this.textNormalizer = textNormalizer;
        this.sentenceTransformer = sentenceTransformer;
    }

    @Override
    protected List<String> splitText(final String text) {
        if (text.isBlank()) {
            return List.of();
        }

        final String normalizedText = textNormalizer != null ? textNormalizer.apply(text) : text;

        List<String> sentences = splitIntoSentences(normalizedText, Locale.getDefault());
        sentences = sentenceTransformer != null ? sentenceTransformer.apply(sentences) : sentences;

        final List<String> chunks = new ArrayList<>();
        final StringBuilder current = new StringBuilder();
        int currentTokens = 0;

        for (String sentence : sentences) {
            final int numTokens = encoding.countTokens(sentence);

            // final IntArrayList intArrayList = encoding.encode(sentence);
            // final int numTokens = intArrayList.size();
            // final String txt = encoding.decode(intArrayList);

            if (currentTokens + numTokens > tokenCount && !current.isEmpty()) {
                chunks.add(current.toString().strip());
                current.setLength(0);
                currentTokens = 0;
            }

            current.append(sentence).append(" ");
            currentTokens += numTokens;
        }

        if (!current.isEmpty()) {
            chunks.add(current.toString().strip());
        }

        return chunks;
    }

    // if (split.chars().filter(ch -> ch == '(').count() % 2 != 0) {
    //     split = split.substring(0, split.length() - 1) + ").";
    // }
}
