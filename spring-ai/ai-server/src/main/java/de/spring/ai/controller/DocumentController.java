package de.spring.ai.controller;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import de.spring.ai.Utils;
import de.spring.ai.config.Config;
import de.spring.ai.tools.DateTimeTools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.document.Document;
import org.springframework.ai.session.DefaultSessionService;
import org.springframework.ai.session.SessionRepository;
import org.springframework.ai.session.SessionService;
import org.springframework.ai.session.advisor.SessionAdvisors;
import org.springframework.ai.session.advisor.SessionMemoryAdvisor;
import org.springframework.ai.session.compaction.TurnCountTrigger;
import org.springframework.ai.session.compaction.WindowCompactionStrategy;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;

/**
 * <a href="http://localhost:8082/documents/store">store</a><br>
 * <a href="http://localhost:8082/documents/search?query=eulenspiegel&filter=priority&#61;false">search</a><br>
 * <a href="http://localhost:8082/documents?prompt=wer war till eulenspiegel">eulenspiegel</a><br>
 *
 * @author Thomas Freese
 */
@RestController
@RequestMapping("docs")
public class DocumentController {
    private static final Logger LOGGER = LoggerFactory.getLogger(DocumentController.class);

    private static final boolean WRITE_DATABASE_TO_FILE = false;

    private final ChatClient chatClient;
    private final ChatModel chatModel;
    private final VectorStore vectorStore;

    public DocumentController(final ChatClient.Builder chatClientBuilder,
                              final ChatModel chatModel,
                              final SessionRepository sessionRepository,
                              final VectorStore vectorStore,
                              @Value("classpath:prompts/systemprompt_entertaining.st") final Resource systemPrompt) {
        Objects.requireNonNull(sessionRepository, "sessionRepository required");

        super();

        final SessionService sessionService = DefaultSessionService.builder()
                .sessionRepository(sessionRepository)
                .build();

        this.chatModel = Objects.requireNonNull(chatModel, "chatModel required");
        this.vectorStore = Objects.requireNonNull(vectorStore, "vectorStore required");

        this.chatClient = chatClientBuilder
                .clone()
                .defaultAdvisors(SessionAdvisors.builder(sessionService)
                        .compaction(
                                new TurnCountTrigger(20), // Auslöser: ab 20 Turns
                                WindowCompactionStrategy.builder().lastTurns(10).build() // Strategie: nur die letzten 10 behalten
                        )
                        .build())
                .defaultAdvisors(
                        QuestionAnswerAdvisor.builder(vectorStore)
                                .order(1)
                                .searchRequest(SearchRequest.builder()
                                        .similarityThresholdAll()
                                        .topK(Config.RAG_MAX_SIMILARITY_RESULTS)
                                        .filterExpression("priority == true")
                                        .build()
                                )
                                .build(),
                        QuestionAnswerAdvisor.builder(vectorStore)
                                .order(2)
                                .searchRequest(SearchRequest.builder()
                                        .similarityThreshold(Config.RAG_MAX_THRESHOLD)
                                        .topK(Config.RAG_MAX_SIMILARITY_RESULTS)
                                        .build()
                                )
                                .build())
                .defaultSystem(systemPrompt)
                .defaultTools(new DateTimeTools())
                .build();
    }

    @GetMapping("/chat")
    @ResponseStatus(HttpStatus.OK)
    public String chat(@RequestParam(value = "prompt") final String prompt, @RequestParam(value = "id", required = false) final String sessionId) {
        LOGGER.info("Execute Prompt: {}", prompt);

        final LocalDateTime start = LocalDateTime.now(ZoneId.systemDefault());

        // UUID.randomUUID().toString()
        final String currentSessionId = sessionId == null ? RequestContextHolder.currentRequestAttributes().getSessionId() : sessionId;

        String content;

        if (sessionId == null) {
            content = chatClient.prompt()
                    .user(prompt)
                    .call()
                    .content();
        }
        else {
            content = chatClient.prompt()
                    .advisors(advisor -> advisor.param(SessionMemoryAdvisor.SESSION_ID_CONTEXT_KEY, currentSessionId))
                    .user(prompt)
                    .call()
                    .content();
        }

        final Duration duration = Duration.between(start, LocalDateTime.now(ZoneId.systemDefault()));
        // final String durationString = "%02d:%02d.%03d".formatted(duration.toMinutes(), duration.toSecondsPart(), duration.toMillisPart());

        if (content != null) {
            content = content
                    .replace(".", ".<br>")
                    .replace("?", "?<br>")
                    .replace("!", "!<br>")
                    .replace(System.lineSeparator(), "<br>");
        }

        if (content == null || content.isBlank()) {
            content = "no content";
        }

        final String result = content;

        return Utils.toHtml(prompt, start, null, sb -> sb.append(result));
    }

    @GetMapping("/embedding")
    public String embedding() {
        final List<String> locationPatterns = new ArrayList<>();
        locationPatterns.add("classpath*:doc-input/**/*.*");
        // locationPatterns.add("file:/FOLDER/**/*.*");

        final LocalDateTime start = LocalDateTime.now(ZoneId.systemDefault());

        final List<Document> documents = DocumentLoader.loadDocuments(chatModel, locationPatterns);

        for (int i = 0; i < documents.size(); i++) {
            final Document document = documents.get(i);
            LOGGER.info("{}. Calling EmbeddingModel for {} - {}/{}",
                    documents.size() - i,
                    document.getMetadata().get("source"),
                    document.getMetadata().get("chunk_index"),
                    document.getMetadata().get("total_chunks"));

            vectorStore.add(List.of(document));
        }

        final Duration duration = Duration.between(start, LocalDateTime.now(ZoneId.systemDefault()));
        final String durationString = "%02d:%02d.%03d".formatted(duration.toMinutes(), duration.toSecondsPart(), duration.toMillisPart());

        return "Documents processed and stored in %s.".formatted(durationString);
    }

    /**
     * RequestParam(value = "filter") final String filter
     */
    @GetMapping("/search")
    @ResponseStatus(HttpStatus.OK)
    public List<Document> search(@RequestParam(value = "query") final String query) {
        return vectorStore.similaritySearch(SearchRequest.builder()
                .similarityThresholdAll()
                .topK(Config.RAG_MAX_SIMILARITY_RESULTS)
                // .filterExpression(filter != null && !filter.isBlank() ? filter : "")
                .query(query)
                .build());
    }

    // @PostConstruct
    @GetMapping("/load")
    @ResponseStatus(HttpStatus.OK)
    void vectorStoreLoad() throws IOException {
        final Resource resource = new ClassPathResource("document-db.json");

        LOGGER.info("Loading vector store file: {}", resource.getFilePath());

        if (vectorStore instanceof final SimpleVectorStore simpleVectorStore) {
            simpleVectorStore.load(resource);
        }
    }

    // @PreDestroy
    @GetMapping("/save")
    @ResponseStatus(HttpStatus.OK)
    void vectorStoreSave() throws IOException {
        final Resource resource = new FileSystemResource("document-db.json");

        LOGGER.info("Save vector store file: {}", resource.getFilePath());

        if (vectorStore instanceof final SimpleVectorStore simpleVectorStore) {
            simpleVectorStore.save(resource.getFile());
        }
    }
}
