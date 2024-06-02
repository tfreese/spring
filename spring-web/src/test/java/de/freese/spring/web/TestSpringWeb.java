// Created: 02 Juni 2024
package de.freese.spring.web;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

/**
 * @author Thomas Freese
 */
@SpringBootTest(properties = "server.port=0", webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = SpringBootWebApplication.class)
@ActiveProfiles("test")
class TestSpringWeb {
    private static HttpClient.Builder httpClientBuilder;

    @BeforeAll
    static void beforeAll() {
        httpClientBuilder = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .followRedirects(HttpClient.Redirect.NEVER)
                .connectTimeout(Duration.ofSeconds(3));
    }

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @LocalServerPort
    private int localServerPort;

    @Test
    void testContextLoads() {
        // Empty
    }

    @Test
    void testMetrics() throws IOException, InterruptedException {
        try (HttpClient httpClient = httpClientBuilder.build()) {
            final HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:" + localServerPort + contextPath + "/actuator/metrics"))
                    .GET()
                    //.header("Accept", "application/json")
                    .timeout(Duration.ofSeconds(3))
                    .build();

            final HttpResponse<String> httpResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

            assertNotNull(httpResponse);
            assertEquals(200, httpResponse.statusCode());
            assertNotNull(httpResponse.body());
            assertFalse(httpResponse.body().isBlank());
        }
    }
}
