package de.spring.ai.controller;

import java.io.File;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import de.spring.ai.Utils;
import de.spring.ai.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;

/**
 * @author Thomas Freese
 */
@RestController
@RequestMapping("docs")
public class DocumentController {
    private static final Logger LOGGER = LoggerFactory.getLogger(DocumentController.class);

    private final ChatClient chatClient;
    private final ChatMemoryRepository chatMemoryRepository;
    private final ChatModel chatModel;
    private final VectorStore vectorStore;

    public DocumentController(final ChatClient.Builder chatClientBuilder,
                              final ChatModel chatModel,
                              final ChatMemoryRepository chatMemoryRepository,
                              final VectorStore vectorStore,
                              @Value("classpath:prompts/systemprompt_entertaining.st") final Resource systemPrompt) {
        super();

        this.chatMemoryRepository = Objects.requireNonNull(chatMemoryRepository, "chatMemoryRepository required");

        final ChatMemory chatMemory = MessageWindowChatMemory.builder()
                .maxMessages(Config.MEMORY_MAX_MESSAGES)
                .chatMemoryRepository(chatMemoryRepository)
                .build();

        this.chatModel = Objects.requireNonNull(chatModel, "chatModel required");
        this.vectorStore = Objects.requireNonNull(vectorStore, "vectorStore required");

        this.chatClient = chatClientBuilder
                .clone()
                .defaultAdvisors(MessageChatMemoryAdvisor
                                .builder(chatMemory)
                                .order(1)
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
                .build();
    }

    @GetMapping("/chat")
    public String chat(@RequestParam(value = "prompt") final String prompt, @RequestParam(value = "id", required = false) final String conversationId) {
        LOGGER.info("Execute Prompt: {}", prompt);

        final LocalDateTime start = LocalDateTime.now();

        // UUID.randomUUID().toString()
        final String currentConversationId = conversationId == null ? RequestContextHolder.currentRequestAttributes().getSessionId() : conversationId;

        String content = chatClient.prompt()
                .advisors(advisor -> advisor.param(ChatMemory.CONVERSATION_ID, currentConversationId))
                .user(prompt)
                .call()
                .content();

        // Do some presentation cosmetics for the content.
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

        final LocalDateTime start = LocalDateTime.now();

        final List<Document> documents = new DocumentLoader().loadDocuments(chatModel, locationPatterns);

        for (int i = 0; i < documents.size(); i++) {
            final Document document = documents.get(i);
            LOGGER.info("{}. Calling EmbeddingModel for {} - {}/{}",
                    documents.size() - i,
                    document.getMetadata().get("source"),
                    document.getMetadata().get("chunk_index"),
                    document.getMetadata().get("total_chunks"));

            vectorStore.add(List.of(document));
        }

        final Duration duration = Duration.between(start, LocalDateTime.now());
        final String durationString = "%02d:%02d.%03d".formatted(duration.toMinutes(), duration.toSecondsPart(), duration.toMillisPart());

        return "Documents processed and stored in %s.".formatted(durationString);
    }

    @GetMapping("/chat/history")
    public List<Message> history() {
        return chatMemoryRepository.findConversationIds().stream().flatMap(id -> chatMemoryRepository.findByConversationId(id).stream()).toList();
    }

    @GetMapping("/chat/historyId")
    public List<Message> historyById(@RequestParam("id") final String id) {
        return chatMemoryRepository.findByConversationId(id);
    }

    @DeleteMapping("/chat/history/delete")
    public void historyDelete() {
        chatMemoryRepository.findConversationIds().forEach(chatMemoryRepository::deleteByConversationId);
    }

    @GetMapping("/search")
    public List<Document> search(@RequestParam(value = "query") final String query) {
        return vectorStore.similaritySearch(SearchRequest.builder()
                .similarityThresholdAll()
                .topK(Config.RAG_MAX_SIMILARITY_RESULTS)
                .query(query)
                .build());
    }

    // @PostConstruct
    @GetMapping("/load")
    @ResponseStatus(HttpStatus.OK)
    void vectorStoreLoad() {
        final String file = "document-db.json";

        LOGGER.info("Loading vector store file: {}", file);

        if (vectorStore instanceof final SimpleVectorStore simpleVectorStore) {
            simpleVectorStore.load(new FileSystemResource(file));
        }
    }

    // @PreDestroy
    @GetMapping("/save")
    @ResponseStatus(HttpStatus.OK)
    void vectorStoreSave() {
        final String file = "document-db.json";

        LOGGER.info("Save vector store file: {}", file);

        if (vectorStore instanceof final SimpleVectorStore simpleVectorStore) {
            simpleVectorStore.save(new File(file));
        }
    }
}
