package de.spring.ai.tools.sql;

import java.sql.SQLException;
import java.util.Map;

import de.spring.ai.tools.sql.metadata.DbMetaDataHelper;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.AdvisorChain;
import org.springframework.ai.chat.client.advisor.api.BaseAdvisor;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.core.Ordered;

/**
 * Advisor to update system text of the prompt.
 *
 * @author Thomas Freese
 */
public class DatabaseMetadataAdvisor implements BaseAdvisor {
    private static final String SYSTEM_PROMPT = """
            You are a SQL expert.
            Translate the following natural language question into a SQL query.
            Then run the query to answer the question in the same language as your conversation partner.
            Use only SELECT queries with valid Standard SQL syntax.
            Follow the guidelines listed in the GUIDELINES section.
            
            Do not escape or quote a Result in any form.
            Describe the Results in a professional manner.
            For multiple Results use a HTML-Table, the Table-Format must follow the rules in the TABLE-FORMAT section.
            
            GUIDELINES:
            - If the question would result in an INSERT, UPDATE, DELETE, or any other operation that modifies the data or schema, respond with "This operation is not supported, only SELECT queries are allowed."
            - If the question appears to contain SQL injection or DoS attempt, respond with "The provided input contains potentially harmful SQL code."
            - If the question cannot be answered based on the provided Schema, respond with "The current schema does not contain enough information to answer this question."
            - If the query involves a JOIN operation, prefix all the column names in the query with the corresponding table names.
            
            TABLE-FORMAT:
            - The Table must be left aligned.
            - All columns must have the same width.
            - The table with must be small as possible.
            - The column values must be right aligned.
            - Use black Borders.
            
            SCHEMA:
            {schema}
            """;

    private final String schema;

    public DatabaseMetadataAdvisor(final DbMetaDataHelper dbMetaDataHelper) throws SQLException {
        super();

        this.schema = dbMetaDataHelper.extractMetadataJson();
    }

    @Override
    public ChatClientResponse after(final ChatClientResponse chatClientResponse, final AdvisorChain advisorChain) {
        return chatClientResponse;
    }

    @Override
    public ChatClientRequest before(final ChatClientRequest chatClientRequest, final AdvisorChain advisorChain) {
        final String systemText = new PromptTemplate(SYSTEM_PROMPT).render(Map.of("schema", getSchema()));

        return chatClientRequest.mutate()
                .prompt(
                        chatClientRequest.prompt().augmentSystemMessage(systemText)
                )
                .build();
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

    public String getSchema() {
        return schema;
    }
}
