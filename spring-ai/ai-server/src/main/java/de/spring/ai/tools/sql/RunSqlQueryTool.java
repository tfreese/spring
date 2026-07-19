package de.spring.ai.tools.sql;

import java.io.IOException;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TimeZone;
import java.util.function.Function;

import javax.sql.DataSource;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.QuoteMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.simple.JdbcClient;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.ObjectWriter;
import tools.jackson.databind.SequenceWriter;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.cfg.DateTimeFeature;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.dataformat.csv.CsvMapper;
import tools.jackson.dataformat.csv.CsvSchema;
import tools.jackson.dataformat.csv.CsvWriteFeature;

/**
 * Use {@linkplain JdbcClient} to run SQL query and output result in CSV or JSON format.
 *
 * @author Thomas Freese
 */
public class RunSqlQueryTool implements Function<RunSqlQueryRequest, RunSqlQueryResponse> {

    private static final String ERROR_ONLY_SELECT_ALLOWED = "Only a single SELECT statement is allowed.";
    private static final String ERROR_QUERY_REQUIRED = "A SQL query is required.";
    private static final String ERROR_QUERY_TOO_LONG = "The SQL query is too long.";
    private static final String ERROR_SYSTEM_SCHEMA_ACCESS = "Access to system schemas is not allowed.";
    private static final String ERROR_UNSAFE_CLAUSE = "The SQL statement contains a forbidden clause or command.";
    private static final Set<String> FORBIDDEN_SYSTEM_SCHEMA_PREFIXES = Set.of(
            "INFORMATION_SCHEMA", "PG_CATALOG", "SYS", "SYSIBM", "SYSCAT", "SYSCS", "SYSFUN", "SYSSTAT"
    );
    private static final Set<String> FORBIDDEN_TOKENS = Set.of(
            "ALTER", "ATTACH", "CALL", "COMMENT", "COMMIT", "COPY", "CREATE", "DELETE", "DETACH", "DO", "DROP",
            "EXEC", "EXECUTE", "EXPLAIN", "GRANT", "INSERT", "MERGE", "REPLACE", "REVOKE", "ROLLBACK", "SAVEPOINT",
            "SET", "SHOW", "TRUNCATE", "UPDATE", "UPSERT", "USE", "VACUUM"
    );
    private static final Logger LOGGER = LoggerFactory.getLogger(RunSqlQueryTool.class);
    private static final int MAX_QUERY_LENGTH = 4_000;
    private static final int MAX_RESULT_ROWS = 200;
    private static final int QUERY_TIMEOUT_SECONDS = 5;

    private static String validateQuery(final String query) {
        if (query == null || query.isBlank()) {
            throw new IllegalArgumentException(ERROR_QUERY_REQUIRED);
        }

        final String trimmedQuery = query.trim();

        if (trimmedQuery.length() > MAX_QUERY_LENGTH) {
            throw new IllegalArgumentException(ERROR_QUERY_TOO_LONG);
        }

        final String[] tokens = query.split("\\s+", -1);

        if (tokens.length == 0 || !"SELECT".equals(tokens[0])) {
            throw new IllegalArgumentException(ERROR_ONLY_SELECT_ALLOWED);
        }

        final String paddedQuery = " " + query + " ";

        if (paddedQuery.contains(" FOR UPDATE ") || paddedQuery.contains(" INTO ")) {
            throw new IllegalArgumentException(ERROR_UNSAFE_CLAUSE);
        }

        for (String token : tokens) {
            if (FORBIDDEN_TOKENS.contains(token)) {
                throw new IllegalArgumentException(ERROR_UNSAFE_CLAUSE);
            }

            final String schemaToken = token.contains(".") ? token.substring(0, token.indexOf('.')) : token;

            if (FORBIDDEN_SYSTEM_SCHEMA_PREFIXES.contains(schemaToken)) {
                throw new IllegalArgumentException(ERROR_SYSTEM_SCHEMA_ACCESS);
            }
        }

        return trimmedQuery;
    }

    private final DataSource dataSource;

    public RunSqlQueryTool(final DataSource dataSource) {
        super();

        this.dataSource = dataSource;
    }

    @Override
    public RunSqlQueryResponse apply(final RunSqlQueryRequest request) {
        try {
            final String validatedQuery = validateQuery(Objects.requireNonNull(request, "request required").query().toUpperCase(Locale.ROOT));

            LOGGER.info("SQL query: {}", validatedQuery);

            final List<Map<String, Object>> result = executeSelect(validatedQuery);

            if (result.isEmpty()) {
                return new RunSqlQueryResponse(null, null);
            }

            final String resultString = toCsvApache(result);

            return new RunSqlQueryResponse(resultString, null);
        }
        catch (IllegalArgumentException ex) {
            LOGGER.warn("Rejected SQL query: {}", ex.getMessage());

            return new RunSqlQueryResponse(null, ex.getMessage());
        }
        catch (SQLException ex) {
            LOGGER.warn("Failed to execute SQL query", ex);

            return new RunSqlQueryResponse(null, "Failed to execute SQL query.");
        }
        catch (Exception ex) {
            LOGGER.warn("Unexpected SQL tool error", ex);

            return new RunSqlQueryResponse(null, "Failed to execute SQL query.");
        }
    }

    private List<Map<String, Object>> executeSelect(final String query) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            connection.setReadOnly(true);

            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setFetchSize(MAX_RESULT_ROWS);
                preparedStatement.setMaxRows(MAX_RESULT_ROWS);
                preparedStatement.setQueryTimeout(QUERY_TIMEOUT_SECONDS);

                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    return mapRows(resultSet);
                }
            }
        }
    }

    private List<Map<String, Object>> mapRows(final ResultSet resultSet) throws SQLException {
        final ResultSetMetaData metaData = resultSet.getMetaData();
        final int columnCount = metaData.getColumnCount();
        final List<Map<String, Object>> rows = new ArrayList<>();

        while (resultSet.next()) {
            final Map<String, Object> row = new LinkedHashMap<>();

            for (int index = 1; index <= columnCount; index++) {
                row.put(metaData.getColumnLabel(index), resultSet.getObject(index));
            }

            rows.add(row);
        }

        return rows;
    }

    private String toCsvApache(final List<Map<String, Object>> result) throws IOException {
        final List<String> fields = result.getFirst().keySet().stream().sorted().toList();

        final CSVFormat csvFormat = CSVFormat.DEFAULT.builder()
                .setHeader(fields.toArray(new String[0]))
                .setQuote('"')
                .setQuoteMode(QuoteMode.ALL)
                .setDelimiter(',')
                .setRecordSeparator(System.lineSeparator())
                .get();

        final StringBuilder stringBuilder = new StringBuilder();

        try (CSVPrinter csvPrinter = csvFormat.print(stringBuilder)) {
            for (Map<String, Object> row : result) {
                csvPrinter.printRecord(fields.stream().map(row::get).toArray());
            }

            csvPrinter.flush();
        }

        LOGGER.info("Apache CSV: {}", stringBuilder);

        return stringBuilder.toString();
    }

    private String toCsvJackson(final List<Map<String, Object>> result) {
        final List<String> fields = result.getFirst().keySet().stream().sorted().toList();

        final CsvSchema.Builder csvSchemaBuilder = CsvSchema.builder()
                .setUseHeader(true)
                .setStrictHeaders(true)
                .setColumnSeparator(',')
                .setQuoteChar('"')
                .setLineSeparator(System.lineSeparator())
                // .setEscapeChar('\\')
                ;

        fields.forEach(csvSchemaBuilder::addColumn);

        final ObjectWriter objectWriter = new CsvMapper().writerFor(Map.class)
                .with(csvSchemaBuilder.build())
                .with(CsvWriteFeature.ALWAYS_QUOTE_EMPTY_STRINGS)
                .with(CsvWriteFeature.ALWAYS_QUOTE_NUMBERS)
                .with(CsvWriteFeature.ALWAYS_QUOTE_STRINGS);

        final StringWriter writer = new StringWriter();

        try (SequenceWriter sequenceWriter = objectWriter.writeValues(writer)) {
            for (Map<String, Object> row : result) {
                sequenceWriter.write(row);
            }

            sequenceWriter.flush();
        }

        writer.flush();

        LOGGER.info("Jackson CSV: {}", writer);

        return writer.toString();
    }

    private String toJson(final List<Map<String, Object>> result) {
        final JsonMapper jsonMapper = JsonMapper.builder()
                .changeDefaultPropertyInclusion(value -> value.withValueInclusion(JsonInclude.Include.NON_EMPTY))
                .defaultTimeZone(TimeZone.getTimeZone(ZoneId.systemDefault()))
                .enable(SerializationFeature.INDENT_OUTPUT)
                .enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT)
                .disable(DateTimeFeature.WRITE_DATES_AS_TIMESTAMPS)
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
                .build();

        final String json = jsonMapper.writeValueAsString(result);

        LOGGER.info("JSON: {}", json);

        return json;
    }
}
