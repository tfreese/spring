// Created: 23.11.2024
package de.freese.spring.thymeleaf.rest;

import java.io.IOException;
import java.net.URI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.DefaultResponseErrorHandler;

/**
 * @author Thomas Freese
 */
public final class NoOpResponseErrorHandler extends DefaultResponseErrorHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(NoOpResponseErrorHandler.class);

    @Override
    protected void handleError(final ClientHttpResponse response, final HttpStatusCode statusCode, final URI url, final HttpMethod method) throws IOException {
        // To read the Response is only possible ONCE!
        // This Method would throw Exceptions, but the TestCases are validated by Assertions.
        
        LOGGER.error("{} - {} - {}", statusCode, method, url);

        // try {
        //     super.handleError(response, statusCode, url, method);
        // }
        // catch (RestClientResponseException ex) {
        //     LOGGER.error(ex.getMessage());
        // }

        // try {
        // ApiError apiError = TestRestApi.this.objectMapper.readValue(exception.getResponseBodyAsByteArray(), ApiError.class);
        // // exception.setStackTrace(apiError.getStackTrace());
        // System.err.println(apiError);
        // }
        // catch (Exception ex) {
        //    // Ignore
        // }
    }
}
