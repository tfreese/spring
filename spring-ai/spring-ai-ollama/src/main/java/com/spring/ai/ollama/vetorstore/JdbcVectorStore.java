// Created: 28.06.2025
package com.spring.ai.ollama.vetorstore;

import static org.springframework.ai.vectorstore.SimpleVectorStore.EmbeddingMath.cosineSimilarity;

import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.content.Media;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.observation.conventions.VectorStoreProvider;
import org.springframework.ai.observation.conventions.VectorStoreSimilarityMetric;
import org.springframework.ai.util.JacksonUtils;
import org.springframework.ai.vectorstore.AbstractVectorStoreBuilder;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.filter.FilterExpressionConverter;
import org.springframework.ai.vectorstore.filter.converter.SimpleVectorStoreFilterExpressionConverter;
import org.springframework.ai.vectorstore.observation.AbstractObservationVectorStore;
import org.springframework.ai.vectorstore.observation.VectorStoreObservationContext;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.MimeType;

/**
 * @author Thomas Freese
 * @see SimpleVectorStore
 */
public final class JdbcVectorStore extends AbstractObservationVectorStore {
    private static final Logger LOGGER = LoggerFactory.getLogger(JdbcVectorStore.class);

    public static final class JdbcVectorStoreBuilder extends AbstractVectorStoreBuilder<JdbcVectorStoreBuilder> {
        private JdbcTemplate jdbcTemplate;

        private JdbcVectorStoreBuilder(final EmbeddingModel embeddingModel) {
            super(embeddingModel);
        }

        @Override
        public JdbcVectorStore build() {
            Objects.requireNonNull(jdbcTemplate, "jdbcTemplate required");

            return new JdbcVectorStore(this);
        }

        public JdbcVectorStoreBuilder jdbcTemplate(final JdbcTemplate jdbcTemplate) {
            this.jdbcTemplate = jdbcTemplate;

            return this;
        }
    }

    public static JdbcVectorStore.JdbcVectorStoreBuilder builder(final EmbeddingModel embeddingModel) {
        return new JdbcVectorStore.JdbcVectorStoreBuilder(embeddingModel);
    }

    private final ExpressionParser expressionParser;
    private final FilterExpressionConverter filterExpressionConverter;
    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;
    private final Map<String, JdbcVectorStoreContent> store = new ConcurrentHashMap<>();
    private final TransactionTemplate transactionTemplate;

    private JdbcVectorStore(final JdbcVectorStore.JdbcVectorStoreBuilder builder) {
        super(builder);

        this.objectMapper = JsonMapper.builder().addModules(JacksonUtils.instantiateAvailableModules()).build();
        this.expressionParser = new SpelExpressionParser();
        this.filterExpressionConverter = new SimpleVectorStoreFilterExpressionConverter();
        jdbcTemplate = builder.jdbcTemplate;
        transactionTemplate = new TransactionTemplate(new DataSourceTransactionManager(jdbcTemplate.getDataSource()));
    }

    @Override
    public VectorStoreObservationContext.Builder createObservationContextBuilder(final String operationName) {
        return VectorStoreObservationContext.builder(VectorStoreProvider.SIMPLE.value(), operationName)
                .dimensions(embeddingModel.dimensions())
                .collectionName("jdbc-vectorstore")
                .similarityMetric(VectorStoreSimilarityMetric.COSINE.value());
    }

    public void deleteAll() {
        final String sql = """
                delete from
                    SPRING_AI_DOCUMENT
                """;

        transactionTemplate.execute(status -> {
                    final int affectedRows = jdbcTemplate.update(sql);

                    LOGGER.info("deleted rows: {}", affectedRows);

                    store.clear();

                    return null;
                }
        );
    }

    @Override
    public void doAdd(final List<Document> documents) {
        Objects.requireNonNull(documents, "Documents list cannot be null");

        if (documents.isEmpty()) {
            throw new IllegalArgumentException("Documents list cannot be empty");
        }

        final ObjectWriter objectWriter = objectMapper.writerWithDefaultPrettyPrinter();

        for (Document document : documents) {
            LOGGER.info("Calling EmbeddingModel for document id: {}", document.getId());

            final float[] embedding = embeddingModel.embed(document);

            final JdbcVectorStoreContent storeContent = new JdbcVectorStoreContent();
            storeContent.setId(document.getId());
            storeContent.setMedia(document.getMedia());
            storeContent.setMetadata(document.getMetadata());
            storeContent.setText(document.getText());
            storeContent.setEmbedding(embedding);

            final String sql = """
                    insert into
                        SPRING_AI_DOCUMENT
                        (ID, MIMETYPE, METADATA, TEXT, EMBEDDING)
                    values
                        (?, ?, ?, ?, ?)
                    """;
            transactionTemplate.execute(status -> {
                        jdbcTemplate.update(sql, ps -> {
                            try {
                                ps.setString(1, storeContent.getId());
                                ps.setString(2, Optional.ofNullable(storeContent.getMedia()).map(Media::getMimeType).map(MimeType::toString).orElse(null));
                                ps.setString(3, objectWriter.writeValueAsString(storeContent.getMetadata()));
                                ps.setString(4, document.getText());
                                ps.setString(5, objectWriter.writeValueAsString(storeContent.getEmbedding()));
                            }
                            catch (JsonProcessingException ex) {
                                throw new RuntimeException("Error serializing to JSON.", ex);
                            }
                        });

                        store.put(document.getId(), storeContent);

                        return null;
                    }
            );
        }
    }

    @Override
    public void doDelete(final List<String> idList) {
        final String sql = """
                delete from
                    SPRING_AI_DOCUMENT
                where
                    ID = ?
                """;

        transactionTemplate.execute(status -> {
                    final int[][] affectedRowsBatch = jdbcTemplate.batchUpdate(sql, idList, 50, (ps, id) -> ps.setString(1, id));
                    final int affectedRows = Arrays.stream(affectedRowsBatch).flatMapToInt(Arrays::stream).sum();

                    LOGGER.info("deleted rows: {}", affectedRows);

                    for (String id : idList) {
                        store.remove(id);
                    }

                    return null;
                }
        );
    }

    @Override
    public List<Document> doSimilaritySearch(final SearchRequest request) {
        final Predicate<JdbcVectorStoreContent> documentFilterPredicate = doFilterPredicate(request);
        final float[] userQueryEmbedding = getUserQueryEmbedding(request.getQuery());

        return store.values()
                .stream()
                .parallel()
                .filter(documentFilterPredicate)
                .map(content -> content.toDocument(cosineSimilarity(userQueryEmbedding, content.getEmbedding())))
                .filter(document -> document.getScore() != null)
                .filter(document -> document.getScore() >= request.getSimilarityThreshold())
                .sorted(Comparator.comparing(Document::getScore).reversed())
                .limit(request.getTopK())
                .toList();
    }

    /**
     * Returns true, if some Documents were loaded.
     */
    public void loadAll() {
        final String sql = """
                select * from
                    SPRING_AI_DOCUMENT
                """;

        final TypeReference<HashMap<String, Object>> typeRefMetaData = new TypeReference<>() {
        };
        final TypeReference<float[]> typeRefEmbedding = new TypeReference<>() {
        };

        store.clear();

        final List<JdbcVectorStoreContent> result = jdbcTemplate.query(sql, (resultSet, rowNum) -> {
            try {
                final JdbcVectorStoreContent storeContent = new JdbcVectorStoreContent();
                storeContent.setId(resultSet.getString("ID"));
                storeContent.setMedia(
                        Optional.ofNullable(resultSet.getString("MIMETYPE"))
                                .map(MimeType::valueOf)
                                .map(mimeType -> Media.builder().mimeType(mimeType).build())
                                .orElse(null));
                storeContent.setMetadata(objectMapper.readValue(resultSet.getString("METADATA"), typeRefMetaData));
                storeContent.setText(resultSet.getString("TEXT"));
                storeContent.setEmbedding(objectMapper.readValue(resultSet.getString("EMBEDDING"), typeRefEmbedding));

                store.put(storeContent.getId(), storeContent);

                return storeContent;
            }
            catch (IOException ex) {
                throw new RuntimeException("Error serializing from JSON.", ex);
            }
        });

        LOGGER.info("loaded documents: {}", result.size());
    }

    public int size() {
        return store.size();
    }

    private Predicate<JdbcVectorStoreContent> doFilterPredicate(final SearchRequest request) {
        if (request.hasFilterExpression()) {
            return document -> {
                final StandardEvaluationContext context = new StandardEvaluationContext();
                context.setVariable("metadata", document.getMetadata());

                return expressionParser
                        .parseExpression(filterExpressionConverter.convertExpression(request.getFilterExpression()))
                        .getValue(context, Boolean.class)
                        ;
            };
        }

        return document -> true;
    }

    private float[] getUserQueryEmbedding(final String query) {
        return embeddingModel.embed(query);
    }
}
