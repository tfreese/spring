// Created: 10.07.2025
package de.spring.ai.chatbot.mcp.client.config;

import java.nio.file.Path;
import java.time.Duration;

import org.neo4j.configuration.GraphDatabaseSettings;
import org.neo4j.configuration.connectors.BoltConnector;
import org.neo4j.configuration.helpers.SocketAddress;
import org.neo4j.cypherdsl.core.renderer.Dialect;
import org.neo4j.dbms.api.DatabaseManagementService;
import org.neo4j.dbms.api.DatabaseManagementServiceBuilder;
import org.neo4j.driver.Driver;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.memory.repository.neo4j.Neo4jChatMemoryRepository;
import org.springframework.ai.chat.memory.repository.neo4j.Neo4jChatMemoryRepositoryConfig;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.TokenCountBatchingStrategy;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.neo4j.Neo4jVectorStore;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;

/**
 * <a href="https://neo4j.com/docs/java-reference/current/java-embedded">neo4j-embedded</a>
 *
 * @author Thomas Freese
 */
@Configuration
@Profile("database")
public class DatabaseConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseConfig.class);

    @Bean
    @DependsOn({"databaseManagementService"})
    ChatMemoryRepository chatMemoryRepository(final Driver driver) {
        return new Neo4jChatMemoryRepository(Neo4jChatMemoryRepositoryConfig.builder()
                .withDriver(driver)
                .build());
    }

    @Bean
    org.neo4j.cypherdsl.core.renderer.Configuration cypherDslConfiguration() {
        return org.neo4j.cypherdsl.core.renderer.Configuration.newConfig().withDialect(Dialect.NEO4J_5).build();
    }

    @Bean(destroyMethod = "shutdown")
    DatabaseManagementService databaseManagementService() {
        final Path pathDB = Path.of(System.getProperty("user.dir"), GraphDatabaseSettings.DEFAULT_DATABASE_NAME);

        final DatabaseManagementService managementService = new DatabaseManagementServiceBuilder(pathDB)
                // .loadPropertiesFromFile(Path.of(pathToConfig + "neo4j.conf"))
                // .setUserLogProvider(new Slf4jLogProvider())
                .setConfig(GraphDatabaseSettings.udc_enabled, false) // Anonymous Usage Data reporting.
                .setConfig(GraphDatabaseSettings.transaction_timeout, Duration.ofSeconds(60L))
                .setConfig(GraphDatabaseSettings.preallocate_logical_logs, true)
                .setConfig(BoltConnector.enabled, true) // For embedded with spring.
                .setConfig(BoltConnector.listen_address, new SocketAddress("localhost", BoltConnector.DEFAULT_PORT)) // For embedded with spring.
                .setConfig(BoltConnector.encryption_level, BoltConnector.EncryptionLevel.DISABLED)
                .build();

        LOGGER.info("managementService created: {}", managementService.listDatabases());

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            LOGGER.info("shutdown");
            managementService.shutdown();
        }));

        return managementService;
    }

    @Bean
    GraphDatabaseService graphDatabaseService(final DatabaseManagementService managementService) {
        final GraphDatabaseService graphDb = managementService.database(GraphDatabaseSettings.DEFAULT_DATABASE_NAME);

        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("graphDb with name '{}' is available: {}", graphDb.databaseName(), graphDb.isAvailable());
        }

        return graphDb;
    }

    @Bean
    @Order(1)
    CommandLineRunner selectAll(final GraphDatabaseService graphDb) {
        // All Properties from all Nodes.
        // String query = "MATCH (n) RETURN n ORDER BY labels(n) asc";

        // Only Property `metadata.source` from Documents.
        final String query = "MATCH (n:Document) RETURN n.`metadata.source` as file ORDER BY file asc";

        return args -> {
            try (Transaction tx = graphDb.beginTx();
                 Result result = tx.execute(query)) {
                LOGGER.info("All Nodes:");

                LOGGER.info(result.resultAsString());

                // result.forEachRemaining(row -> {
                //             for (String key : result.columns()) {
                //                 final Object value = row.get(key);
                //
                //                 if (value instanceof final Node node) {
                //                     // LOGGER.info("Node: {} = {}", key, node.getAllProperties());
                //                 }
                //                 // else if (value instanceof final Relationship rs) {
                //                 //     LOGGER.info("Relationship: {} = {}", key, rs.getAllProperties());
                //                 else {
                //                     LOGGER.info("{} = {}", key, value);
                //                 }
                //             }
                //         }
                // );
            }
        };
    }

    @Bean
    @DependsOn({"databaseManagementService"})
    VectorStore vectorStore(final Driver driver, final EmbeddingModel embeddingModel) {
        return Neo4jVectorStore.builder(driver, embeddingModel)
                // Optional: defaults to "neo4j"
                .databaseName(GraphDatabaseSettings.DEFAULT_DATABASE_NAME)
                // Optional: defaults to COSINE
                .distanceType(Neo4jVectorStore.Neo4jDistanceType.COSINE)
                // Optional: defaults to 1536
                // IllegalArgumentException: Index query vector has 1024 dimensions, but indexed vectors have 1536.
                .embeddingDimension(1024)
                // Optional: defaults to "Document"
                .label("Document")
                // Optional: defaults to "embedding"
                .embeddingProperty("embedding")
                // Optional: defaults to "spring-ai-document-index"
                .indexName("custom-index")
                // Optional: defaults to false
                .initializeSchema(true)
                // Optional: defaults to TokenCountBatchingStrategy
                .batchingStrategy(new TokenCountBatchingStrategy())
                .build();
    }
}
