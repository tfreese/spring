package de.spring.ai.chatbot.mcp.server;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Thomas Freese
 */
public final class Shutdown {
    private static final Logger LOGGER = LoggerFactory.getLogger(Shutdown.class);

    static void main() {
        // curl -X POST localhost:8081/actuator/shutdown
        final URI uri = URI.create("http://localhost:8081/actuator/shutdown");

        try (HttpClient httpClient = HttpClient.newBuilder().build()) {
            final HttpRequest request = HttpRequest.newBuilder(uri)
                    .POST(HttpRequest.BodyPublishers.noBody())
                    .build();

            final HttpResponse<String> httpResponse = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("{}", httpResponse.body());
            }
        } catch (Exception ex) {
            // Restore interrupted state.
            Thread.currentThread().interrupt();

            LOGGER.error(ex.getMessage(), ex);
        }
    }

    private Shutdown() {
        super();
    }
}
