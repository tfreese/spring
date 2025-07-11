package com.spring.ai.ollama.config;

import java.nio.file.Path;
import java.time.Duration;

import org.neo4j.configuration.GraphDatabaseSettings;
import org.neo4j.configuration.connectors.BoltConnector;
import org.neo4j.configuration.helpers.SocketAddress;
import org.neo4j.cypherdsl.core.renderer.Dialect;
import org.neo4j.dbms.api.DatabaseManagementService;
import org.neo4j.dbms.api.DatabaseManagementServiceBuilder;
import org.neo4j.driver.Driver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.memory.repository.neo4j.Neo4jChatMemoryRepository;
import org.springframework.ai.chat.memory.repository.neo4j.Neo4jChatMemoryRepositoryConfig;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.TokenCountBatchingStrategy;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.neo4j.Neo4jVectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("database")
public class DatabaseConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseConfig.class);

    // @Bean
    // @DependsOn({"dataSourceInitializer"})
    // ChatMemoryRepository chatMemoryRepository(final DataSource dataSource, final PlatformTransactionManager txManager) {
    //     return JdbcChatMemoryRepository.builder()
    //             .dataSource(dataSource)
    //             .transactionManager(txManager)
    //             .build();
    // }
    //
    // @Bean
    // DataSourceInitializer dataSourceInitializer(final DataSource dataSource) throws IOException {
    //     final Resource resourceJdbcChatMemoryRepository = new ClassPathResource("/org/springframework/ai/chat/memory/repository/jdbc/schema-hsqldb.sql");
    //
    //     final String sql = resourceJdbcChatMemoryRepository.getContentAsString(StandardCharsets.UTF_8)
    //             .replace("CREATE TABLE", "CREATE TABLE IF NOT EXISTS")
    //             .replace("CREATE INDEX", "CREATE INDEX IF NOT EXISTS")
    //             .replace("ADD CONSTRAINT", "ADD CONSTRAINT IF NOT EXISTS");
    //
    //     final ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
    //     populator.addScript(new ByteArrayResource(sql.getBytes(StandardCharsets.UTF_8)));
    //     populator.addScript(new ClassPathResource("schema-document.sql"));
    //     // populator.execute(dataSource);
    //
    //     final DataSourceInitializer dataSourceInitializer = new DataSourceInitializer();
    //     dataSourceInitializer.setDataSource(dataSource);
    //     dataSourceInitializer.setDatabasePopulator(populator);
    //
    //     return dataSourceInitializer;
    // }

    // @Bean
    // public DataSource getDataSource(@Value("${spring.datasource.username}") final String username,
    //                                 @Value("${spring.datasource.password}") final String password,
    //                                 final PasswordEncoder passwordEncoder) {
    //     final DataSourceBuilder dataSourceBuilder = DataSourceBuilder.create();
    //     dataSourceBuilder.username(username);
    //     dataSourceBuilder.password(password);
    //     // dataSourceBuilder.password(passwordEncoder.decode(password));
    //
    //     return dataSourceBuilder.build();
    // }

    // @Bean
    // @DependsOn({"databaseManagementService"})
    // public Driver driver(@Value("${spring.neo4j.uri}") final String uri) {
    //     // return GraphDatabase.driver("bolt://localhost:7687", AuthTokens.basic("neo4j", "secret"));
    //     return GraphDatabase.driver(uri, AuthTokens.none());
    // }

    // @Bean
    // VectorStore vectorStore(final EmbeddingModel embeddingModel, final JdbcTemplate jdbcTemplate) {
    //     return JdbcVectorStore.builder(embeddingModel).jdbcTemplate(jdbcTemplate).build();
    // }

    @Bean
    @DependsOn({"databaseManagementService"})
    public VectorStore vectorStore(final Driver driver, final EmbeddingModel embeddingModel) {
        return Neo4jVectorStore.builder(driver, embeddingModel)
                // Optional: defaults to "neo4j"
                .databaseName(GraphDatabaseSettings.DEFAULT_DATABASE_NAME)
                // Optional: defaults to COSINE
                .distanceType(Neo4jVectorStore.Neo4jDistanceType.COSINE)
                // Optional: defaults to 1536.
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
        final Path pathDB = Path.of(System.getProperty("java.io.tmpdir"), GraphDatabaseSettings.DEFAULT_DATABASE_NAME);

        final DatabaseManagementService managementService = new DatabaseManagementServiceBuilder(pathDB)
                // .loadPropertiesFromFile(Path.of(pathToConfig + "neo4j.conf"))
                // .setUserLogProvider(new Slf4jLogProvider())
                .setConfig(GraphDatabaseSettings.udc_enabled, false) // Anonymous Usage Data reporting.
                .setConfig(GraphDatabaseSettings.transaction_timeout, Duration.ofSeconds(60L))
                .setConfig(GraphDatabaseSettings.preallocate_logical_logs, true)
                .setConfig(BoltConnector.enabled, true) // For embedded with spring.
                .setConfig(BoltConnector.listen_address, new SocketAddress("localhost", BoltConnector.DEFAULT_PORT)) // For embedded with spring.
                .setConfig(BoltConnector.encryption_level, BoltConnector.EncryptionLevel.DISABLED)
                // .setConfig(HttpConnector.enabled, true)
                .build();

        LOGGER.info("managementService created: {}", managementService.listDatabases());

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            LOGGER.info("shutdown");
            managementService.shutdown();
        }));

        return managementService;
    }

    // @Bean
    // DatabaseSelectionProvider databaseSelectionProvider() {
    //     // return DatabaseSelectionProvider.createStaticDatabaseSelectionProvider("neo4j");
    //
    //     return () -> {
    //         return DatabaseSelection.byName("neo4j");
    //         // return DatabaseSelection.undecided();
    //     };
    // }

    // @Bean
    // GraphDatabaseService graphDatabaseService(final DatabaseManagementService managementService) {
    //     final GraphDatabaseService graphDb = managementService.database(GraphDatabaseSettings.DEFAULT_DATABASE_NAME);
    //
    //     LOGGER.info("graphDb with name '{}' is available: {}", graphDb.databaseName(), graphDb.isAvailable());
    //
    //     return graphDb;
    // }
}
