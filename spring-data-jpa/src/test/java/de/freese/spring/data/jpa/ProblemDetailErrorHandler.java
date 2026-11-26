package de.freese.spring.data.jpa;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpRequest;
import org.springframework.http.ProblemDetail;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.json.JsonMapper;

/**
 * @author Thomas Freese
 * @since 26.11.26
 */
public record ProblemDetailErrorHandler(JsonMapper jsonMapper) implements RestClient.ResponseSpec.ErrorHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(ProblemDetailErrorHandler.class);

    public ProblemDetailErrorHandler(final JsonMapper jsonMapper) {
        this.jsonMapper = Objects.requireNonNull(jsonMapper, "jsonMapper required");
    }

    @Override
    public void handle(final @NonNull HttpRequest request, final @NonNull ClientHttpResponse response) throws IOException {
        String responseBody = null;

        try (InputStream inputStream = response.getBody()) {
            responseBody = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        }
        catch (final Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
        }

        if (responseBody == null || responseBody.isBlank()) {
            throw new RuntimeException("Failed Request without Exception Message");
        }

        try {
            final ProblemDetail problemDetail = jsonMapper.readValue(responseBody, ProblemDetail.class);

            // jsonMapper.writerWithDefaultPrettyPrinter().writeValue(System.out, problemDetail);

            throw ProblemDetailException.of(problemDetail);
        }
        catch (final ProblemDetailException ex) {
            throw ex;
        }
        catch (final Exception ex) {
            LOGGER.error(ex.getMessage(), ex);

            // Not a ProblemDetail.
            throw new RuntimeException(responseBody, ex);
        }
    }
}