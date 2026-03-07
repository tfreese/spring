package de.spring.ai.chatbot.mcp.client.documents;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import de.spring.ai.chatbot.mcp.client.ChatTools;
import de.spring.ai.chatbot.mcp.client.config.ChatConfig;
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
import org.springframework.ai.document.MetadataMode;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.writer.FileDocumentWriter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
@RequestMapping("documents")
public class DocumentController {
    private static final Logger LOGGER = LoggerFactory.getLogger(DocumentController.class);

    private static final boolean WRITE_DATABASE_TO_FILE = false;

    private final ChatClient chatClient;
    private final ChatMemoryRepository chatMemoryRepository;
    private final ChatModel chatModel;
    private final VectorStore vectorStore;

    public DocumentController(final ChatClient.Builder chatClientBuilder,
                              final ChatModel chatModel,
                              final ChatMemoryRepository chatMemoryRepository,
                              final VectorStore vectorStore,
                              @Value("classpath:prompts/document-system.st") final Resource systemPrompt) {
        super();

        this.chatMemoryRepository = Objects.requireNonNull(chatMemoryRepository, "chatMemoryRepository required");

        final ChatMemory chatMemory = MessageWindowChatMemory.builder()
                .maxMessages(ChatConfig.MEMORY_MAX_MESSAGES)
                .chatMemoryRepository(chatMemoryRepository)
                .build();

        this.chatModel = Objects.requireNonNull(chatModel, "chatModel required");
        this.vectorStore = Objects.requireNonNull(vectorStore, "vectorStore required");

        this.chatClient = chatClientBuilder
                .defaultAdvisors(MessageChatMemoryAdvisor
                                .builder(chatMemory)
                                .order(1)
                                .build(),
                        QuestionAnswerAdvisor.builder(vectorStore)
                                .order(2)
                                .searchRequest(SearchRequest.builder()
                                        .similarityThresholdAll()
                                        .topK(ChatConfig.RAG_MAX_SIMILARITY_RESULTS)
                                        .filterExpression("priority == true")
                                        .build()
                                )
                                .build(),
                        QuestionAnswerAdvisor.builder(vectorStore)
                                .order(3)
                                .searchRequest(SearchRequest.builder()
                                        .similarityThreshold(ChatConfig.RAG_MAX_THRESHOLD)
                                        .topK(ChatConfig.RAG_MAX_SIMILARITY_RESULTS)
                                        .build()
                                )
                                .build())
                .defaultSystem(systemPrompt)
                .defaultTools(new ChatTools())
                .build();
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public String chat(@RequestParam(value = "prompt") final String prompt, @RequestParam(value = "id", required = false) final String conversationId) {
        LOGGER.info("Execute Prompt: {}", prompt);

        final LocalDateTime start = LocalDateTime.now();

        // UUID.randomUUID().toString()
        final String currentConversationId = conversationId == null ? RequestContextHolder.currentRequestAttributes().getSessionId() : conversationId;

        String content;

        if (conversationId == null) {
            content = chatClient.prompt()
                    .user(prompt)
                    .call()
                    .content();
        } else {
            content = chatClient.prompt()
                    .advisors(advisor -> advisor.param(ChatMemory.CONVERSATION_ID, currentConversationId))
                    .user(prompt)
                    .call()
                    .content();
        }

        final Duration duration = Duration.between(start, LocalDateTime.now());
        final String durationString = "%02d:%02d.%03d".formatted(duration.toMinutes(), duration.toSecondsPart(), duration.toMillisPart());

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

        final StringBuilder sb = new StringBuilder();
        sb.append("<html>");
        sb.append("<head><title>AI Demo</title></head>");
        sb.append("<body>");
        sb.append("<b>Question: ").append(prompt).append("</b><br>");
        sb.append("<b>Duration: ").append(durationString).append("</b><br>");
        sb.append("<b>Answer:</b><br>");
        sb.append(content);
        sb.append("<br>");
        sb.append("</body>");
        sb.append("</html>");

        return sb.toString();
    }

    @DeleteMapping("/history/delete")
    public void deleteHistory() {
        findAllConversation().forEach(chatMemoryRepository::deleteByConversationId);
    }

    @GetMapping("/history")
    public List<String> findAllConversation() {
        return chatMemoryRepository.findConversationIds();
    }

    @GetMapping("/history/{id}")
    public List<Message> findConversationById(@PathVariable final String id) {
        return chatMemoryRepository.findByConversationId(id);
    }

    /**
     * RequestParam(value = "filter") final String filter
     */
    @GetMapping("/search")
    @ResponseStatus(HttpStatus.OK)
    public List<Document> search(@RequestParam(value = "query") final String query) {
        return vectorStore.similaritySearch(SearchRequest.builder()
                .similarityThresholdAll()
                .topK(ChatConfig.RAG_MAX_SIMILARITY_RESULTS)
                // .filterExpression(filter != null && !filter.isBlank() ? filter : "")
                .query(query)
                .build());
    }

    @GetMapping("/store")
    public String store() {
        final List<String> locationPatterns = new ArrayList<>();
        locationPatterns.add("classpath*:doc-input/**/*.*");

        final LocalDateTime start = LocalDateTime.now();

        final List<Document> documents = new DocumentLoader().loadDocuments(chatModel, locationPatterns);
        writeDocuments(documents);

        final Duration duration = Duration.between(start, LocalDateTime.now());
        final String durationString = "%02d:%02d.%03d".formatted(duration.toMinutes(), duration.toSecondsPart(), duration.toMillisPart());

        return "Documents processed and stored in %s.".formatted(durationString);
    }

    private void writeDocuments(final List<Document> documents) {
        LOGGER.info("Storing documents in {} database.", WRITE_DATABASE_TO_FILE ? "file" : "vector");

        if (WRITE_DATABASE_TO_FILE) {
            final FileDocumentWriter writer = new FileDocumentWriter("documents-db.txt", true, MetadataMode.ALL, false);
            writer.accept(documents);
            LOGGER.info("Documents stored to file database.");

        } else {
            for (int i = 0; i < documents.size(); i++) {
                final Document document = documents.get(i);
                LOGGER.info("{}. Calling EmbeddingModel for document id = {}", documents.size() - i, document.getId());

                vectorStore.add(List.of(document));
            }

            LOGGER.info("Documents stored to vector database.");
        }
    }
}
