package de.spring.ai.controller;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import de.spring.ai.Utils;
import de.spring.ai.tools.sql.DatabaseMetadataAdvisor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * <a href="https://github.com/JavaAIDev/simple-text-to-sql">simple-text-to-sql</a>
 *
 * @author Thomas Freese
 */
@RestController
@RequestMapping("db")
public class DatabaseController {
    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseController.class);

    private final ChatClient chatClient;
    private final DatabaseMetadataAdvisor databaseMetadataAdvisor;

    public DatabaseController(final ChatClient.Builder chatClientBuilder, final DatabaseMetadataAdvisor databaseMetadataAdvisor) {
        super();

        this.chatClient = chatClientBuilder
                .clone()
                .build();
        this.databaseMetadataAdvisor = Objects.requireNonNull(databaseMetadataAdvisor, "databaseMetadataAdvisor required");
    }

    @GetMapping("/chat")
    public String chat(@RequestParam(value = "prompt") final String prompt) {
        LOGGER.info("Execute Prompt: {}", prompt);

        final LocalDateTime start = LocalDateTime.now();

        // final String content = chatClient.prompt()
        //         .user(prompt)
        //         .advisors(databaseMetadataAdvisor)
        //         .toolNames("runSqlQuery")
        //         .options(ChatOptions.builder().model("gpt-4.1").build()) // Overwrite Default, better Results as gpt-4o-mini.
        //         .call()
        //         .content();

        final ChatResponse chatResponse = chatClient.prompt()
                .user(prompt)
                .advisors(databaseMetadataAdvisor)
                .toolNames("runSqlQuery")
                .options(ChatOptions.builder().model("gpt-4.1").build()) // Overwrite Default, better Results as gpt-4o-mini.
                .call()
                .chatResponse();

        if (chatResponse == null) {
            return "No ChatResponse";
        }

        final Usage usage = chatResponse.getMetadata().getUsage();

        final String content = Optional.ofNullable(chatResponse.getResult()).map(Generation::getOutput).map(AssistantMessage::getText).orElse(null);

        return Utils.toHtml(prompt, start, usage, sb -> sb.append(content));
    }

    /**
     * Translate a natural language question into a SQL query.
     */
    @GetMapping("/toSql")
    public String speechToSql(@RequestParam(value = "prompt") final String prompt) {
        LOGGER.info("Execute Prompt: {}", prompt);

        final LocalDateTime start = LocalDateTime.now();

        final String systemPrompt = """
                You are a SQL expert.
                Translate the following natural language question into a SQL query.
                Use only valid Standard SQL syntax.
                Return ONLY the SQL query, no explanation.
                Follow the guidelines listed in the GUIDELINES section.
                
                GUIDELINES:
                - Only produce SELECT queries.
                - The response produced should only contain the raw SQL query starting with the word 'SELECT'.
                - Do not wrap the SQL query in markdown code blocks (```sql or ```).
                - If the question would result in an INSERT, UPDATE, DELETE, or any other operation that modifies the data or schema, respond with "This operation is not supported, only SELECT queries are allowed."
                - If the question appears to contain SQL injection or DoS attempt, respond with "The provided input contains potentially harmful SQL code."
                - If the question cannot be answered based on the provided SCHEMA, respond with "The current schema does not contain enough information to answer this question."
                - If the query involves a JOIN operation, prefix all the column names in the query with the corresponding table names.
                
                SCHEMA:
                {schema}
                """;
        // Hard coded Schema.
        // - CUSTOMER(CUSTOMER_ID, CUSTOMER_NO, NAME, EMAIL, CREATED_AT)
        // - ORDERS(ORDER_ID, CUSTOMER_ID, ORDER_NO, ORDER_DATE, STATUS)
        // - ORDER_ITEM(ORDER_ITEM_ID, ORDER_ID, POSITION_NO, PRODUCT_CODE, QUANTITY, UNIT_PRICE)

        final PromptTemplate promptTemplate = new PromptTemplate(systemPrompt);

        // final String content = chatClient.prompt(promptTemplate.create(Map.of("schema", databaseMetadataAdvisor.getSchema())))
        //         .user(prompt)
        //         .advisors(new SimpleLoggerAdvisor())
        //         .call()
        //         .content();
        
        final ChatResponse chatResponse = chatClient.prompt(promptTemplate.create(Map.of("schema", databaseMetadataAdvisor.getSchema())))
                .user(prompt)
                .advisors(new SimpleLoggerAdvisor())
                .call()
                .chatResponse();

        if (chatResponse == null) {
            return "No ChatResponse";
        }

        final Usage usage = chatResponse.getMetadata().getUsage();

        final String content = Optional.ofNullable(chatResponse.getResult()).map(Generation::getOutput).map(AssistantMessage::getText).orElse(null);

        return Utils.toHtml(prompt, start, usage, sb -> sb.append(content));
    }
}
