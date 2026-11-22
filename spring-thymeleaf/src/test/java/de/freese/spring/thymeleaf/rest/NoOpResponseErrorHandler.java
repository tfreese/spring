package de.freese.spring.thymeleaf.rest;

import java.io.IOException;

import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.RestClient;

/**
 * @author Thomas Freese
 * @since 23.11.2024
 */
public final class NoOpResponseErrorHandler implements RestClient.ResponseSpec.ErrorHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(NoOpResponseErrorHandler.class);

    @Override
    public void handle(final @NonNull HttpRequest request, final @NonNull ClientHttpResponse response) throws IOException {
        LOGGER.error("{} - {} - {}", response.getStatusCode(), request.getMethod(), request.getURI());
    }
}
