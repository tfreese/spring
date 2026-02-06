package de.spring.ai.tools.sql;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

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

    private static final Logger LOGGER = LoggerFactory.getLogger(RunSqlQueryTool.class);

    private final JdbcClient jdbcClient;

    public RunSqlQueryTool(final JdbcClient jdbcClient) {
        super();

        this.jdbcClient = jdbcClient;
    }

    @Override
    public RunSqlQueryResponse apply(final RunSqlQueryRequest request) {
        try {
            LOGGER.info("SQL query: {}", request.query());

            final List<Map<String, Object>> result = jdbcClient.sql(request.query()).query().listOfRows();

            if (result.isEmpty()) {
                return new RunSqlQueryResponse(null, null);
            }

            final String resultString = toCsvApache(result);

            return new RunSqlQueryResponse(resultString, null);
        } catch (Exception ex) {
            return new RunSqlQueryResponse(null, ex.getMessage());
        }
    }

    private String toCsvApache(final List<Map<String, Object>> result) throws IOException {
        final List<String> fields = result.getFirst().keySet().stream().sorted().toList();

        final CSVFormat csvFormat = CSVFormat.DEFAULT.builder()
                .setHeader(fields.toArray(new String[0]))
                .setQuote('"')
                .setQuoteMode(QuoteMode.ALL)
                .setDelimiter(',')
                .setRecordSeparator('\n')
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
                .defaultTimeZone(Calendar.getInstance().getTimeZone())
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
